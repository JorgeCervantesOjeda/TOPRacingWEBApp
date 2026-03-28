# Implementation Notes

## PrimeFaces AJAX Navigation vs Legacy

### Case: `Login -> Create Account`

This project already had a working pattern in the legacy app. When a migrated Jakarta page gets stuck after a PrimeFaces confirm dialog, compare the current rendered button and controller/view flow against the legacy app before adding new navigation logic.

### What worked

- Keep the navigation decision in the controller.
- Let the view materialize navigation through `showUI(...)`.
- Use standard `ExternalContext.redirect(...)` from the view.
- Resolve redirects to the Jakarta Faces routing actually used by this app: `/faces/*.xhtml`.
- For `Create Account`, keep the legacy interaction pattern:
  - `ajax="true"`
  - `immediate="true"`
  - `<p:confirm ... />`
  - `onclick="PF('dlgWait').show();"`
- When comparing a PrimeFaces command button against legacy, inspect the rendered `data-pfconfirmcommand` and `PrimeFaces.ab(...)` payload, not only the XHTML source.

### What did not work

- Do not move the UI decision into the view bean just to force navigation.
- Do not add ad hoc AJAX redirect state like `pendingRedirectTarget` for this flow.
- Do not replace JSF redirect behavior with `PrimeFaces.current().executeScript("window.location=...")` unless there is no cleaner option.
- Do not assume `process="@this"` is harmless on a migrated PrimeFaces button. It changed the request shape and did not match the legacy behavior here.

### Project rule for similar cases

When a migrated button used to work in the legacy app:

1. Compare `xhtml`, controller, and view bean with the legacy version.
2. Preserve the legacy controller/view responsibilities.
3. Restore the legacy PrimeFaces request shape first.
4. Only then adapt URL resolution for Jakarta routing differences.
5. Prefer the smallest fix that restores the original behavior.

### Quick checklist

- Is the controller still calling `showUI(...)`?
- Is the view only executing navigation, not deciding it?
- Is the redirect a real JSF redirect?
- Does the target URL include `/faces/` when needed in this app?
- Does the rendered `PrimeFaces.ab(...)` match the legacy button closely enough?
- Did the server log confirm the action method actually ran?
