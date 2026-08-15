# Versioned Rules Acceptance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add current-rules consultation and versioned acceptance before operational participation.

**Architecture:** Store rules acceptance on `Tables.Participant` as accepted version plus timestamp, derive operational confirmation from e-mail confirmation, usable PayPal, and current rules acceptance, and expose the current rules in the profile with a PrimeFaces modal. Keep the gate centralized in `Controller.isValidParticipant()` and login, following the app's current JSF/PrimeFaces and Hibernate mapping patterns.

**Tech Stack:** Java 11, Jakarta Faces, PrimeFaces 10, Hibernate 5.6, MySQL, JUnit 5, Mockito, Maven WAR project.

## Global Constraints

- SRS v2.7 requires current rules to be consultable before acceptance and from the participant profile.
- SRS v2.7 requires acceptance to record participant, accepted version, and date/time.
- Protected operations must be blocked without current rules acceptance.
- Existing users begin with null acceptance fields and must accept current rules before protected operations.
- The modal text is a system rule summary, not country-specific legal advice.
- Do not implement country-specific legal text, real payments, refunds, PayPal captures, release publishing, or GitHub/Zenodo publication.
- Follow project formatting: spaces inside non-empty parentheses and continued method chains with leading dots.
- Do not use silent fallbacks.

---

### Task 1: Participant Rules Acceptance Model

**Files:**
- Modify: `src/main/java/Tables/Participant.java`
- Modify: `src/main/java/Tables/Participant.hbm.xml`
- Create: `src/test/java/Tables/ParticipantTermsAcceptanceTest.java`
- Create: `docs/schema/2026-08-14-participant-terms-acceptance.sql`

**Interfaces:**
- Produces: `Participant.CURRENT_TERMS_VERSION`, `Participant.CURRENT_TERMS_EFFECTIVE_DATE`, `Participant.acceptCurrentTerms()`, `Participant.hasAcceptedCurrentTerms()`, `Participant.getTermsVersionAccepted()`, `Participant.setTermsVersionAccepted( String )`, `Participant.getTermsAcceptedAt()`, `Participant.setTermsAcceptedAt( Date )`.
- Consumes: existing `Participant.refreshOperationalConfirmation()`.

- [ ] **Step 1: Write failing model tests**

Add `src/test/java/Tables/ParticipantTermsAcceptanceTest.java`:

```java
package Tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ParticipantTermsAcceptanceTest {

  @Test
  void operationalConfirmationRequiresCurrentTermsAcceptance() {
    Participant participant = new Participant();
    participant.setEmailConfirmed( true );
    participant.setPaypalUsable( true );

    participant.refreshOperationalConfirmation();

    assertFalse( participant.isConfirmed() );
    assertNull( participant.getConfirmedAt() );

    participant.acceptCurrentTerms();
    participant.refreshOperationalConfirmation();

    assertTrue( participant.isConfirmed() );
    assertNotNull( participant.getConfirmedAt() );
  }

  @Test
  void staleAcceptedTermsDoNotSatisfyCurrentTerms() {
    Participant participant = new Participant();
    participant.setTermsVersionAccepted( "SRS-2.6-legacy" );
    participant.setTermsAcceptedAt( new java.util.Date() );

    assertFalse( participant.hasAcceptedCurrentTerms() );
  }

  @Test
  void acceptCurrentTermsRecordsVersionAndTimestamp() {
    Participant participant = new Participant();

    participant.acceptCurrentTerms();

    assertEquals( Participant.CURRENT_TERMS_VERSION,
                  participant.getTermsVersionAccepted() );
    assertNotNull( participant.getTermsAcceptedAt() );
    assertTrue( participant.hasAcceptedCurrentTerms() );
  }
}
```

- [ ] **Step 2: Run model test to verify it fails**

Run: `mvn -Dtest=Tables.ParticipantTermsAcceptanceTest test`

Expected: compilation failure because the terms methods and fields do not exist.

- [ ] **Step 3: Implement minimal participant model**

Add nullable fields, getters, setters, `acceptCurrentTerms()`, `hasAcceptedCurrentTerms()`, and update `refreshOperationalConfirmation()` to require current terms. When `setConfirmed( true )` is called by legacy tests/helpers, also call `acceptCurrentTerms()` so existing confirmed fixtures remain operational.

- [ ] **Step 4: Map fields and document SQL**

Add Hibernate properties:

```xml
<property name="termsVersionAccepted" type="string">
    <column name="terms_version_accepted" />
</property>
<property name="termsAcceptedAt" type="timestamp">
    <column name="terms_accepted_at" length="19" />
</property>
```

Create SQL documentation:

```sql
-- docs/schema/2026-08-14-participant-terms-acceptance.sql
-- Adds versioned rules acceptance fields required by SRS v2.7.

ALTER TABLE participant
  ADD COLUMN terms_version_accepted VARCHAR(64) NULL,
  ADD COLUMN terms_accepted_at DATETIME NULL;
```

- [ ] **Step 5: Run model test to verify it passes**

Run: `mvn -Dtest=Tables.ParticipantTermsAcceptanceTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add src/main/java/Tables/Participant.java src/main/java/Tables/Participant.hbm.xml src/test/java/Tables/ParticipantTermsAcceptanceTest.java docs/schema/2026-08-14-participant-terms-acceptance.sql
git commit -m "feat: add participant terms acceptance state"
```

### Task 2: Operational Gate For Current Rules Acceptance

**Files:**
- Modify: `src/main/java/Controller/UI.java`
- Modify: `src/main/java/Controller/Controller.java`
- Modify: `src/main/java/View/ViewBean.java`
- Modify: `src/main/java/View/BundleViewEnglish.properties`
- Modify: `src/main/java/View/BundleViewSpanish.properties`
- Modify: `src/test/java/Controller/ControllerTest.java`

**Interfaces:**
- Consumes: `Participant.hasAcceptedCurrentTerms()`, `Participant.refreshOperationalConfirmation()`.
- Produces: `UI.ERROR_TERMS_ACCEPTANCE_REQUIRED`.

- [ ] **Step 1: Write failing controller test**

Add to `ControllerTest`:

```java
@Test
void clickLoginRequiresCurrentTermsAfterPaypalConfirmation() {
  Participant attempt = participant( 10L,
                                     false );
  attempt.setEmailConfirmed( true );
  attempt.setPaypalUsable( true );
  attempt.setTermsVersionAccepted( "SRS-2.6-legacy" );
  attempt.setTermsAcceptedAt( new java.util.Date() );
  when( modelBean.getValidParticipant( attempt ) ).thenReturn( attempt );

  controller.clickLogin( attempt );

  verify( modelBean,
          never() ).sendEmail( any(),
                               any(),
                               anyLong() );
  verify( modelBean,
          never() ).incNumUsuariosActivos();
  verify( view ).showUI( UI.ERROR_TERMS_ACCEPTANCE_REQUIRED,
                         attempt );
}
```

Update `clickLoginCreatesSessionOnlyForOperationallyConfirmedUsers()` so the participant accepts current terms before login:

```java
attempt.acceptCurrentTerms();
```

- [ ] **Step 2: Run controller test to verify it fails**

Run: `mvn -Dtest=Controller.ControllerTest#clickLoginRequiresCurrentTermsAfterPaypalConfirmation test`

Expected: compilation failure because `UI.ERROR_TERMS_ACCEPTANCE_REQUIRED` does not exist, or behavioral failure because login does not show that UI.

- [ ] **Step 3: Implement login and protected-action gate**

Add `UI.ERROR_TERMS_ACCEPTANCE_REQUIRED`. In `Controller.clickLogin()`, after PayPal check and before successful login, call `refreshOperationalConfirmation()` and show `ERROR_TERMS_ACCEPTANCE_REQUIRED` when `!currentParticipant.hasAcceptedCurrentTerms()`.

In `isValidParticipant()`, after credential validation, refresh operational confirmation and show `ERROR_TERMS_ACCEPTANCE_REQUIRED` when the current participant lacks current terms acceptance.

- [ ] **Step 4: Add visible error message**

Add `ViewBean.showUI()` case for `ERROR_TERMS_ACCEPTANCE_REQUIRED` using bundle keys:

```properties
ERROR TERMS ACCEPTANCE REQUIRED=Current rules acceptance required
ERROR TERMS ACCEPTANCE REQUIRED LONG=Review and accept the current TOP Racing rules from your profile before using protected operations.
```

Spanish:

```properties
ERROR TERMS ACCEPTANCE REQUIRED=Falta aceptar las reglas vigentes
ERROR TERMS ACCEPTANCE REQUIRED LONG=Consulta y acepta las reglas vigentes de TOP Racing desde tu perfil antes de usar operaciones protegidas.
```

- [ ] **Step 5: Run controller test to verify it passes**

Run: `mvn -Dtest=Controller.ControllerTest test`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add src/main/java/Controller/UI.java src/main/java/Controller/Controller.java src/main/java/View/ViewBean.java src/main/java/View/BundleViewEnglish.properties src/main/java/View/BundleViewSpanish.properties src/test/java/Controller/ControllerTest.java
git commit -m "feat: require current rules acceptance for operations"
```

### Task 3: Profile Rules Modal And Acceptance UI

**Files:**
- Modify: `src/main/java/View/EditParticipantBean.java`
- Modify: `src/main/webapp/editparticipant.xhtml`
- Modify: `src/main/java/View/BundleViewEnglish.properties`
- Modify: `src/main/java/View/BundleViewSpanish.properties`
- Create: `src/test/java/View/EditParticipantPageMarkupTest.java`
- Create: `src/test/java/View/EditParticipantBeanTest.java`

**Interfaces:**
- Consumes: `Participant.CURRENT_TERMS_VERSION`, `Participant.CURRENT_TERMS_EFFECTIVE_DATE`, `Participant.acceptCurrentTerms()`, `Participant.hasAcceptedCurrentTerms()`.
- Produces: `EditParticipantBean.getCurrentTermsVersion()`, `EditParticipantBean.getCurrentTermsEffectiveDate()`, `EditParticipantBean.getCurrentTermsText()`, `EditParticipantBean.isAcceptCurrentTerms()`, `EditParticipantBean.setAcceptCurrentTerms( boolean )`.

- [ ] **Step 1: Write failing bean test**

Add `EditParticipantBeanTest` with direct setter injection:

```java
package View;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import Controller.Controller;
import Tables.Participant;
import org.junit.jupiter.api.Test;

class EditParticipantBeanTest {

  @Test
  void termsAcceptanceSelectionRecordsCurrentVersionBeforeSave() {
    ViewBean viewBean = mock( ViewBean.class );
    Controller controller = mock( Controller.class );
    Participant participant = new Participant();
    EditParticipantBean bean = new EditParticipantBean();
    bean.setViewBean( viewBean );
    when( viewBean.getController() ).thenReturn( controller );
    when( viewBean.getCurrentParticipant() ).thenReturn( participant );

    bean.init();
    bean.setAcceptCurrentTerms( true );
    bean.clickSave();

    assertEquals( Participant.CURRENT_TERMS_VERSION,
                  participant.getTermsVersionAccepted() );
    assertNotNull( participant.getTermsAcceptedAt() );
  }

  @Test
  void currentTermsTextIncludesEconomicConsequences() {
    EditParticipantBean bean = new EditParticipantBean();

    assertTrue( bean.getCurrentTermsText()
      .contains( "no automatic refund" ) );
  }
}
```

- [ ] **Step 2: Write failing markup test**

Add `EditParticipantPageMarkupTest`:

```java
package View;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class EditParticipantPageMarkupTest {

  @Test
  void profileShowsCurrentRulesModalAndAcceptanceCheckbox() throws Exception {
    String markup = Files.readString( Path.of(
      "src/main/webapp/editparticipant.xhtml" ) );

    assertTrue( markup.contains( "currentRulesDialog" ) );
    assertTrue( markup.contains( "acceptCurrentTerms" ) );
    assertTrue( markup.contains( "getCurrentTermsText" )
                || markup.contains( "currentTermsText" ) );
    assertTrue( markup.contains( "PF('currentRulesDialog').show()" ) );
  }
}
```

- [ ] **Step 3: Run UI tests to verify they fail**

Run: `mvn -Dtest=View.EditParticipantBeanTest,View.EditParticipantPageMarkupTest test`

Expected: compilation failure because bean methods do not exist and markup lacks modal IDs.

- [ ] **Step 4: Implement bean behavior**

In `EditParticipantBean`, add boolean `acceptCurrentTerms`; initialize it from `p.hasAcceptedCurrentTerms()` in `init()`. Add getters for version, effective date, and rules text. In `clickSave()`, if `acceptCurrentTerms` is true, call `p.acceptCurrentTerms()` before delegating to controller.

- [ ] **Step 5: Implement profile markup**

In `editparticipant.xhtml`, add a "Current rules" section with version/date, `p:commandButton` opening `PF('currentRulesDialog').show()`, `p:selectBooleanCheckbox` bound to `#{editParticipantBean.acceptCurrentTerms}`, and a `p:dialog widgetVar="currentRulesDialog"` showing `#{editParticipantBean.currentTermsText}`.

- [ ] **Step 6: Add bundle text**

Add concise English and Spanish labels for current rules, effective date, view button, acceptance checkbox, and modal text. The Spanish copy should use ordinary Spanish and avoid uncommon literal translations.

- [ ] **Step 7: Run UI tests to verify they pass**

Run: `mvn -Dtest=View.EditParticipantBeanTest,View.EditParticipantPageMarkupTest test`

Expected: PASS.

- [ ] **Step 8: Commit**

Run:

```bash
git add src/main/java/View/EditParticipantBean.java src/main/webapp/editparticipant.xhtml src/main/java/View/BundleViewEnglish.properties src/main/java/View/BundleViewSpanish.properties src/test/java/View/EditParticipantBeanTest.java src/test/java/View/EditParticipantPageMarkupTest.java
git commit -m "feat: show and accept current rules in profile"
```

### Task 4: Final Verification

**Files:**
- Review all files changed by Tasks 1-3.

**Interfaces:**
- Consumes: all produced interfaces above.
- Produces: verified build and clean worktree except intentional generated files, if any.

- [ ] **Step 1: Run focused tests**

Run:

```bash
mvn -Dtest=Tables.ParticipantTermsAcceptanceTest,Controller.ControllerTest,View.EditParticipantBeanTest,View.EditParticipantPageMarkupTest test
```

Expected: PASS.

- [ ] **Step 2: Run full unit suite**

Run:

```bash
mvn test
```

Expected: PASS.

- [ ] **Step 3: Run formatting checks**

Run:

```bash
git diff --check
rg -n "traducciones engañosas" src docs
```

Expected: no diff-check errors and no unwanted terminology hits.

- [ ] **Step 4: Summarize status**

Report commits created, tests run, and any remaining deployment or database migration action the user must consciously choose before release.

## Self-Review

- Spec coverage: model fields, current-version derivation, protected-action block, profile consultation, modal, acceptance checkbox, SQL documentation, and tests are covered.
- Placeholder scan: no placeholders are intentionally left.
- Type consistency: all planned method names are produced before they are consumed.
