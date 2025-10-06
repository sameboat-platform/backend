# PR Checklist

- [ ] Description includes context and intended outcome
- [ ] Tests updated or added (if applicable)
- [ ] CHANGELOG.md updated under [Unreleased] (user-visible changes)
- [ ] CI: typecheck, lint, tests, and build pass
- [ ] Labels applied (e.g., `type:*`, `area:*`)
- [ ] Linked issues (e.g., Fixes #123)

## Post-merge actions

- [ ] Confirm required checks are still enforced on main (CI, coverage)
- [ ] If this PR is a release: push tags with `git push --follow-tags`
- [ ] If release: create GitHub Release (optional) and close milestone; move leftovers to next milestone
- [ ] Any follow-up issues created and linked

## Notes

Add any deployment risks, feature flags, or follow-ups here.
# Pull Request

## Summary

Describe the change concisely.

## Type of Change

-   [ ] Feature
-   [ ] Fix
-   [ ] Refactor
-   [ ] Documentation
-   [ ] Chore / Internal

## Changelog

-   [ ] I added an entry under `[Unreleased]` in `CHANGELOG.md` (or this PR does not warrant a changelog line).

## Testing

Describe how you tested this (or include screenshots/logs if UI/behavioral change).

## Checklist

-   [ ] Types pass (`npm run typecheck`)
-   [ ] Lint passes (`npm run lint`)
-   [ ] Tests pass (`npm test`)
-   [ ] No unused exports / dead code introduced

## Follow-Up Tasks (Optional)

List any deferred work.
