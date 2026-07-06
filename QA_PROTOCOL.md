# 🛡️ Vanguard QA Protocol: CLI Regression

## 1. The Mandatory Gate
Before any merge to `main` that affects the CLI or Sync Logic, the **Manual Regression Suite** must be executed and pass.

### Execution Command:
```bash
mvn clean package -DskipTests
./scripts/cli-regression.sh
```

## 2. Certification Criteria
- [ ] **Result:** Must return `RESULT: PASS 🟢`.
- [ ] **Filesystem:** Source file count must exactly match destination file count in the `test-dataset`.
- [ ] **Logs:** No `[VANGUARD-FATAL]` or `IOException` logs permitted in the output.

## 3. Failure Protocol
If the script returns `FAIL 🔴`:
1. **Isolate:** Identify if the failure is in the logic or the test dataset.
2. **Fix:** Apply surgical patch.
3. **Re-verify:** Run the suite until a green signal is achieved.
4. **Report:** Document the failure and the fix in the PR description.

---
*Failure to execute this gate is a breach of the Zero-Defect Deployment mandate.*
