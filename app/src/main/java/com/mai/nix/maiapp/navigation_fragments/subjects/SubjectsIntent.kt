package com.mai.nix.maiapp.navigation_fragments.subjects

sealed class SubjectsIntent {
    data class LoadSubjects(val group: String): SubjectsIntent()
    data class SetWeek(val week: Int): SubjectsIntent()
}