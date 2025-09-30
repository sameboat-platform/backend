# 🛟 Git Recovery Checklist

## 1. Throw away all unstaged changes
```bash
git restore .
```

## 2. Throw away staged + unstaged changes
```bash
git restore --staged .
git restore .
```

## 3. Reset back to last good commit (wipe local changes)
```bash
git reset --hard HEAD
```

## 4. Undo the last commit (keep changes in working dir)
```bash
git reset --soft HEAD~1
```

## 5. Undo the last commit (discard changes)
```bash
git reset --hard HEAD~1
```

## 6. Recover a specific file from last commit
```bash
git restore <path/to/file>
```

## 7. Safe checkpoint before experiments
```bash
git add .
git commit -m "checkpoint before risky change"
```

## 8. Make a throwaway branch for experiments
```bash
git switch -c experiment/<name>
```

---

# 🚦 Decision Tree: Which Command to Use?

- 🔴 **"I broke stuff, but didn’t commit yet"**  
  → Use **#1** if only unstaged, or **#2** if staged too.

- 🔴 **"I committed to the wrong branch, but want to keep the code"**  
  → Use **#4** (`--soft HEAD~1`) to uncommit but keep the changes.

- 🔴 **"I committed to the wrong branch, and don’t want the code"**  
  → Use **#5** (`--hard HEAD~1`) to erase that commit completely.

- 🔴 **"I want to throw away *all* local edits and go back clean"**  
  → Use **#3** (`git reset --hard HEAD`).

- 🔴 **"Only one file is messed up"**  
  → Use **#6** (`git restore <file>`).

- 🟢 **"About to try something risky"**  
  → Use **#7** to checkpoint, or **#8** to try in a safe branch.
