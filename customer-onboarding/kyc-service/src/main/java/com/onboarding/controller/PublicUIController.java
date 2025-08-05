package com.onboarding.controller;

import com.onboarding.model.KycApplication;
import com.onboarding.service.KycApplicationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui/register")
@SessionAttributes("kycApplication") // This annotation makes the "kycApplication" object available across multiple requests in the user's session.
public class PublicUIController {
    
    private final KycApplicationService kycApplicationService;

    public PublicUIController(KycApplicationService kycAppService) {
        this.kycApplicationService = kycAppService;
    }

    // This method ensures a fresh KycApplication object is created at the start of the session.
    @ModelAttribute("kycApplication")
    public KycApplication kycApplication() {
        return new KycApplication();
    }

    // --- STEP 1: Personal Info ---
    @GetMapping // The journey starts here: GET /ui/register
    public String showPersonalInfoForm(Model model, @ModelAttribute("kycApplication") KycApplication kycApplication) {
        model.addAttribute("kycApplication", kycApplication);
        return "ui/register-step1-personal";
    }

    @PostMapping("/personal-info")
    public String processPersonalInfo(@Valid @ModelAttribute("kycApplication") KycApplication kycApplication, BindingResult result) {
        if (result.hasFieldErrors("fullName") || result.hasFieldErrors("dob") || result.hasFieldErrors("gender") ||
            result.hasFieldErrors("maritalStatus") || result.hasFieldErrors("fathersName") || result.hasFieldErrors("nationality") ||
            result.hasFieldErrors("profession") || result.hasFieldErrors("address")) {
            return "ui/register-step1-personal"; // If errors on this page, stay here
        }
        return "redirect:/ui/register/documents"; // Go to step 2
    }

    // --- STEP 2: Documents ---
    @GetMapping("/documents")
    public String showDocumentsForm(Model model, @ModelAttribute("kycApplication") KycApplication kycApplication) {
        model.addAttribute("kycApplication", kycApplication);
        return "ui/register-step2-documents";
    }

    @PostMapping("/documents")
    public String processDocuments(@ModelAttribute("kycApplication") KycApplication kycApplication) {
        // We can add validation for file uploads if needed, but for now we proceed
        return "redirect:/ui/register/finish"; // Go to step 3
    }
    
    // --- STEP 3: Final Account & Login Details ---
    @GetMapping("/finish")
    public String showFinishForm(Model model, @ModelAttribute("kycApplication") KycApplication kycApplication) {
        model.addAttribute("kycApplication", kycApplication);
        return "ui/register-step3-finish";
    }

    @PostMapping("/finish")
    public String processFinish(
        @Valid @ModelAttribute("kycApplication") KycApplication kycApplication,
        BindingResult result,
        SessionStatus sessionStatus,
        RedirectAttributes redirectAttributes) {
            
        if (result.hasErrors()) {
            return "ui/register-step3-finish";
        }
        
        try {
            kycApplicationService.submitApplication(kycApplication);
            sessionStatus.setComplete(); // IMPORTANT: Clears the "kycApplication" object from the session after submission.
            redirectAttributes.addFlashAttribute("successMessage", "Application submitted successfully! You will be notified by email once your KYC is reviewed.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Submission failed: " + e.getMessage());
            return "redirect:/ui/register"; // Start over on error
        }
    }
}