package com.example.myapplication.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudSyncPlanTest {
    private fun revision(digest: String) = SyncRevision("deck", digest)

    @Test fun acceptsSingleSidedChange() {
        assertEquals(SyncDecision.UPLOAD_LOCAL,
            resolveSync(revision("old"), revision("new"), revision("old")))
        assertEquals(SyncDecision.DOWNLOAD_REMOTE,
            resolveSync(revision("old"), revision("old"), revision("new")))
    }

    @Test fun rejectsConcurrentEditsAndUnknownBaseline() {
        assertEquals(SyncDecision.CONFLICT,
            resolveSync(revision("old"), revision("local"), revision("remote")))
        assertEquals(SyncDecision.CONFLICT,
            resolveSync(null, revision("local"), revision("remote")))
        assertEquals(SyncDecision.CONFLICT,
            resolveSync(revision("old"), null, revision("old")))
    }

    @Test fun handlesFirstSyncWithoutDroppingData() {
        assertEquals(SyncDecision.LOCAL_ONLY, resolveSync(null, revision("new"), null))
        assertEquals(SyncDecision.REMOTE_ONLY, resolveSync(null, null, revision("cloud")))
        assertEquals(SyncDecision.UNCHANGED,
            resolveSync(null, revision("same"), revision("same")))
    }

    @Test fun allConflictsMustBeResolvedBeforeApplyingPlan() {
        val plan = calculateSyncPlan(
            listOf(revision("old")),
            listOf(revision("local")),
            listOf(revision("cloud"))
        )
        assertFalse(plan.safeToApply)
        assertTrue("deck" in plan.conflicts)
    }
}
