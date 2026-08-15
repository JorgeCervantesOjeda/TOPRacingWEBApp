# Versioned Rules Acceptance Design

## Goal

Implement versioned acceptance of current TOP Racing rules before operational participation, while allowing users to consult the current rules before accepting them and later from their profile.

## Requirement Analysis

Observation: SRS v2.7 requires explicit acceptance of the current terms and conditions before operational participation. It also requires the current rules to be consultable before acceptance and from the participant profile, showing version, effective date, and full text.

Assumption: In the current Java/MySQL app, `Participant.confirmed` is the operational account flag derived from e-mail confirmation and usable PayPal status. This feature will extend that derivation to include current rules acceptance.

Inference: The most coherent implementation is to keep login credentials, e-mail confirmation, PayPal onboarding, and rules acceptance as separate observable states, while using one central operational gate for protected actions.

No contradiction found: the SRS allows public consultation without acceptance and requires protected operations to be blocked without current acceptance.

## Recommended Design

The profile page will show a compact "Current rules" section with the active rules version and effective date. A "View current rules" button will open a PrimeFaces modal dialog containing the full current rules text. The same section will include an explicit acceptance checkbox tied to the current rules version.

When the participant saves the profile with the acceptance checkbox selected, the system stores the current version and acceptance timestamp. If the current version later changes, the participant is no longer operational until the new version is accepted.

The controller will continue using the central participant validation path for protected actions. Login will first check credentials, e-mail confirmation, and PayPal status; then it will check current rules acceptance before opening the welcome page. Protected actions that already call the participant validity helper will also be blocked when the current participant lacks current acceptance.

## Data Model

Add these nullable fields to `Tables.Participant`:

- `termsVersionAccepted: String`
- `termsAcceptedAt: Date`

Add a model-level current rules version and effective date:

- `CURRENT_TERMS_VERSION = "SRS-2.7-2026-08-14"`
- `CURRENT_TERMS_EFFECTIVE_DATE = "2026-08-14"`

The participant has current acceptance when `termsVersionAccepted` equals `CURRENT_TERMS_VERSION` and `termsAcceptedAt` is not null.

## UI Behavior

The profile form displays:

- current rules version;
- current rules effective date;
- a button to open the full rules modal;
- an acceptance checkbox;
- a visible message when acceptance is required.

The modal text is a system rule summary, not country-specific legal advice. It must include the economic consequences required by SRS v2.7: refunds, no automatic refund after disqualification for breach, unsafe conduct, fraud, non-delivery, tampering, rule violation, blocks, defaults, and exclusions.

## Persistence

Add SQL documentation under `docs/schema` for:

- `participant.terms_version_accepted`;
- `participant.terms_accepted_at`.

Existing participants should start with null values and must accept the current version before protected operations.

## Tests

Add focused automated tests for:

- a participant with e-mail and PayPal but without current rules acceptance cannot complete login;
- a participant with current rules acceptance can complete login;
- current operational confirmation requires e-mail, PayPal, and current rules acceptance;
- accepting the current rules records version and timestamp;
- stale accepted version is not considered current;
- profile markup exposes rules version, rules modal trigger, and acceptance checkbox.

## Out Of Scope

This feature does not implement country-specific legal text, real payments, refunds, PayPal captures, release publishing, or GitHub/Zenodo publication. Country-specific legal validation remains an open decision before real-money operation in each jurisdiction.
