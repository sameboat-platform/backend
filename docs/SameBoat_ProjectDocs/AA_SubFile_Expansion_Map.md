# Sub-file Expansion Map

_Last updated: 2025-08-19_

## Purpose
Start with a single master file, then "graduate" sections into dedicated files when they exceed usefulness-in-one-place. Keep within ~20-file cap by consolidating and using temp slots.

## Graduation Triggers (Rules of Thumb)
- **Length:** Section > ~600–800 words or requires its own TOC.
- **Change rate:** Section edited weekly or by multiple contributors.
- **Dependencies:** Other docs frequently link to this section.
- **Decisions:** Section holds policies/algorithms that need version history.

## Mapping
- From **ProjectOverview** → split into:
  - Roles → [01_UserRoles.md]
  - Matching → [02_MatchingLogic.md]
  - Journey → [03_UserJourney.md]
  - Features → [04_Features.md]
  - UI/UX → [05_UI_UX_Concepts.md]
  - Brand/Content → [06_Branding_Messaging.md], [07_Content_Strategy.md]
  - Tech → [08_SystemArchitecture.md], [09_Data_Model.md], [10_Security_Privacy.md], [11_Scalability.md]
  - Growth/Governance → [12_Roadmap.md], [13_Moderation_Policies.md], [14_Partnerships.md], [15_Metrics_Success.md]

## Housekeeping Conventions
- **Frontmatter (optional):**
  ```yaml
  title: SameBoat — <Doc Title>
  updated: 2025-08-19
  status: draft
  owner: Nick
  ```
- **Links:** Relative (`[Matching](02_MatchingLogic.md)`).
- **IDs:** Use `## Heading` anchors for stable references.
- **Changelogs:** Append `## Changelog` with date bullets for key decisions.
- **Temp Slots:** Use 19/20 for in-progress; migrate when stable and free the slot.

## Example Flow
1. Write everything in `00_ProjectOverview.md`.
2. Matching grows → cut/paste into `02_MatchingLogic.md`; leave a summary + link in Overview.
3. Need structured metrics → create `15_Metrics_Success.md` and link from Overview.
4. Out of slots? Consolidate: merge 06+07 into one, or archive old temp files offline.
