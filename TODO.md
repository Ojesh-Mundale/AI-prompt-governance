# TODO: Enterprise Secret-Masking & Prompt Governance

## Steps

- [x] Step 0: Complete Gemini → Ollama migration (previous task)
- [ ] Step 1: Rewrite `RiskAnalyzerUtil.java` with masking + 15+ regex patterns
- [ ] Step 2: Update `Analysis.java` entity with `maskedPrompt` and `detectedSecretTypes` fields
- [ ] Step 3: Update `schema.sql` with new columns
- [ ] Step 4: Update `PromptServiceImpl.java` to mask prompts before sending to Ollama
- [ ] Step 5: Update `PromptController.java` to pass masked prompt to view
- [ ] Step 6: Update `view.html` with Detected Secrets, Masked Prompt, and improved UI
