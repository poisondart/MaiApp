package com.mai.nix.maiapp.navigation_fragments.exams

import android.app.Application
import com.mai.nix.maiapp.MaiApp
import com.mai.nix.maiapp.helpers.Parser
import com.mai.nix.maiapp.model.ExamModel

class ExamsRepository {
    suspend fun getExamsWeb(group: String) = arrayListOf(ExamModel("fds", "fsd", "dad", "das", "dsad", "dsadas"),
            ExamModel("fds2", "fsd2", "dad2", "das2", "dsad2", "dsadas2"))
    suspend fun getExamsDb(application: Application) = (application as MaiApp).getDatabase().examDao.getExams()
}