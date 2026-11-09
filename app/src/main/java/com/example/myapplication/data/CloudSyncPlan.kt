package com.example.myapplication.data

data class SyncRevision(val id: String, val digest: String) {
    init { require(id.isNotBlank() && digest.isNotBlank()) }
}

enum class SyncDecision {
    UNCHANGED, UPLOAD_LOCAL, DOWNLOAD_REMOTE, LOCAL_ONLY, REMOTE_ONLY, CONFLICT
}

/** Não resolve divergência por "última gravação vence" sem evidência de base comum. */
fun resolveSync(
    previous: SyncRevision?,
    local: SyncRevision?,
    remote: SyncRevision?
): SyncDecision {
    require(listOfNotNull(previous, local, remote).map { it.id }.distinct().size <= 1)
    if (local == null && remote == null) return SyncDecision.UNCHANGED
    if (local == null) return if (previous == null) SyncDecision.REMOTE_ONLY else SyncDecision.CONFLICT
    if (remote == null) return if (previous == null) SyncDecision.LOCAL_ONLY else SyncDecision.CONFLICT
    if (local.digest == remote.digest) return SyncDecision.UNCHANGED
    if (previous == null) return SyncDecision.CONFLICT
    return when {
        local.digest == previous.digest -> SyncDecision.DOWNLOAD_REMOTE
        remote.digest == previous.digest -> SyncDecision.UPLOAD_LOCAL
        else -> SyncDecision.CONFLICT
    }
}

data class SyncPlan(val decisions: Map<String, SyncDecision>) {
    val conflicts: Set<String>
        get() = decisions.filterValues { it == SyncDecision.CONFLICT }.keys
    val safeToApply: Boolean get() = conflicts.isEmpty()
}

/** O chamador mantém os dados íntegros até o usuário resolver conflitos. */
fun calculateSyncPlan(
    previous: List<SyncRevision>,
    local: List<SyncRevision>,
    remote: List<SyncRevision>
): SyncPlan {
    fun asMap(items: List<SyncRevision>): Map<String, SyncRevision> {
        require(items.map { it.id }.distinct().size == items.size)
        return items.associateBy { it.id }
    }
    val a = asMap(previous)
    val b = asMap(local)
    val c = asMap(remote)
    val allIds = (a.keys + b.keys + c.keys).toSortedSet()
    return SyncPlan(allIds.associateWith { id -> resolveSync(a[id], b[id], c[id]) })
}
