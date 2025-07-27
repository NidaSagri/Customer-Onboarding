import os
import requests
import google.generativeai as genai
from flask import Flask, request, jsonify
from flask_cors import CORS
from datetime import datetime, date, timedelta
import json

# --- Configuration ---
app = Flask(__name__)
CORS(app) 
genai.configure(api_key=os.environ.get("GOOGLE_API_KEY"))
SPRING_BOOT_API_URL = "http://localhost:8080/api/chatbot"

# --- Tool Definitions (These are correct) ---
search_customer_tool = { "name": "search_customer", "description": "Primary tool to get all information about a single customer by searching with name, email, PAN, or ID. Retrieves personal info, KYC status, and all account details.", "parameters": {"type": "OBJECT", "properties": {"search_term": {"type": "STRING", "description": "The customer's name, email, ID, or PAN."}}, "required": ["search_term"]} }
get_dashboard_statistics_tool = { "name": "get_dashboard_statistics", "description": "Get key statistics from the admin dashboard.", "parameters": {"type": "OBJECT", "properties": {}} }
list_customers_by_kyc_status_tool = { "name": "list_customers_by_kyc_status", "description": "List the names of customers with a specific KYC status.", "parameters": { "type": "OBJECT", "properties": {"kyc_status": {"type": "STRING", "description": "Must be one of: PENDING, VERIFIED, REJECTED."}}, "required": ["kyc_status"]} }
list_accounts_by_date_tool = { "name": "list_accounts_by_date", "description": "Get a list of bank accounts created on a specific date (e.g., 'today', 'yesterday').", "parameters": {"type": "OBJECT", "properties": {"date_query": {"type": "STRING", "description": "A date reference like 'today'."}}, "required": ["date_query"]} }

# --- Helper Functions ---
def serialize_history(history):
    # This function is correct.
    serializable_history = []
    for content in history:
        message_parts = []
        for part in content.parts:
            part_dict = {}
            if hasattr(part, 'text') and part.text: part_dict['text'] = part.text
            if hasattr(part, 'function_call') and part.function_call: part_dict['function_call'] = {'name': part.function_call.name, 'args': dict(part.function_call.args)}
            if hasattr(part, 'function_response') and part.function_response: part_dict['function_response'] = {'name': part.function_response.name, 'response': dict(part.function_response.response)}
            if part_dict: message_parts.append(part_dict)
        if message_parts: serializable_history.append({'role': content.role, 'parts': message_parts})
    return serializable_history

# --- FINAL, INTELLIGENT FORMATTER ---
def format_customer_details(data):
    customer = data.get('customer')
    if not customer: return "No customer data found."
    
    kyc_status = customer.get('kycStatus')
    response_lines = [ f"Here are the details for {customer['fullName']} (ID: {customer['id']}):", f"KYC Status: {kyc_status}" ]
    
    account = data.get('account')
    if account:
        account_status = account.get('accountStatus')
        response_lines.append("\n--- Account Information ---")
        
        # --- NEW, EXPLICIT BUSINESS LOGIC ---
        # Always state the account status explicitly.
        response_lines.append(f"Account Status: {account_status}")
        
        # Then, provide details based on the status.
        if kyc_status == 'REJECTED':
            response_lines.append("Further account details (like balance) are hidden because the customer's KYC was rejected.")
        elif account_status == 'INACTIVE':
            response_lines.append(f"Account Number: {account.get('accountNumber')}")
            response_lines.append("The account is not yet active. Balance is not applicable.")
        else: # Account is ACTIVE and KYC is VERIFIED
            response_lines.extend([
                f"Account Number: {account.get('accountNumber')}",
                f"Account Type: {account.get('accountType')}",
                f"Current Balance: ₹{account.get('balance')}"
            ])
    else:
        response_lines.append("\nThis customer does not have a bank account.")
        
    return "\n".join(response_lines)

def format_account_list(accounts, date_str):
    if not accounts: return f"No new accounts were created on {date_str}."
    response_lines = [f"Here are the accounts created on {date_str}:"]
    for acc in accounts:
        line = f"- Account #{acc['accountNumber']} ({acc['accountType']}) for Customer ID: {acc['customerId']}"
        response_lines.append(line)
    return "\n".join(response_lines)

# --- Chatbot Logic ---
@app.route('/chat', methods=['POST'])
def chat():
    data = request.json
    user_query = data.get('query')
    role = data.get('role')
    history = data.get('history', [])
    
    # We will only handle the ADMIN role for this final version to keep it simple
    if not user_query or role != 'ADMIN':
        return jsonify({"error": "This endpoint is for ADMIN users only."}), 400

    tools_for_model = [search_customer_tool, get_dashboard_statistics_tool, list_customers_by_kyc_status_tool, list_accounts_by_date_tool]
    
    try:
        # Correct model name
        model = genai.GenerativeModel(model_name='gemini-2.5-flash', tools=tools_for_model)
        chat_session = model.start_chat(history=history)
        response = chat_session.send_message(user_query)
        
        if not response.candidates[0].content.parts or not response.candidates[0].content.parts[0].function_call:
            updated_history = serialize_history(chat_session.history)
            return jsonify({"response": response.text, "history": updated_history})

        function_call = response.candidates[0].content.parts[0].function_call
        api_response_text = "Sorry, I couldn't process that request."
        tool_name = function_call.name
        auth_credentials = ('admin', 'password')
        
        api_res = None
        if tool_name == 'search_customer':
            search_term = function_call.args['search_term']
            api_url = f"{SPRING_BOOT_API_URL}/admin/search-customer?keyword={search_term}"
            api_res = requests.get(api_url, auth=auth_credentials)
        elif tool_name == 'get_dashboard_statistics':
            api_url = f"{SPRING_BOOT_API_URL}/admin/dashboard-stats"
            api_res = requests.get(api_url, auth=auth_credentials)
        elif tool_name == 'list_customers_by_kyc_status':
            status = function_call.args['kyc_status']
            api_url = f"{SPRING_BOOT_API_URL}/admin/list-by-kyc?status={status}"
            api_res = requests.get(api_url, auth=auth_credentials)
        elif tool_name == 'list_accounts_by_date':
            date_query = function_call.args['date_query'].lower()
            target_date = date.today()
            if 'yesterday' in date_query:
                target_date = date.today() - timedelta(days=1)
            try:
                target_date = datetime.strptime(date_query, '%Y-%m-%d').date()
            except ValueError:
                pass
            date_str = target_date.isoformat()
            api_url = f"{SPRING_BOOT_API_URL}/admin/accounts-created-on-date?date={date_str}"
            api_res = requests.get(api_url, auth=auth_credentials)

        # Process API response
        if api_res and api_res.status_code == 200:
            data = api_res.json()
            if tool_name == 'get_dashboard_statistics':
                api_response_text = f"Dashboard stats: Total: {data['total']}, Pending: {data['pending']}, Verified: {data['verified']}, Rejected: {data['rejected']}."
            elif tool_name == 'list_customers_by_kyc_status':
                customer_list = [f"- {c['fullName']} (ID: {c['id']})" for c in data] if data else []
                api_response_text = f"Here are some customers with {status} status:\n" + "\n".join(customer_list) if customer_list else f"No customers were found with status '{status}'."
            elif tool_name == 'list_accounts_by_date':
                api_response_text = format_account_list(data, date_query)
            else: # Handles search_customer
                api_response_text = format_customer_details(data)
        elif api_res:
            api_response_text = f"Sorry, I encountered an error (Status: {api_res.status_code})."

        final_response = chat_session.send_message({"function_response": {"name": tool_name, "response": {"result": api_response_text}}})
        updated_history = serialize_history(chat_session.history)
        return jsonify({"response": final_response.text, "history": updated_history})

    except Exception as e:
        print(f"An unexpected error occurred in the chat logic: {e}")
        return jsonify({"response": "An unexpected error occurred while processing your request."}), 500

if __name__ == '__main__':
    app.run(port=5000, debug=True)