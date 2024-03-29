package com.mai.nix.maiapp.navigation_fragments.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mai.nix.maiapp.MaiApp
import com.mai.nix.maiapp.R
import com.mai.nix.maiapp.choose_groups.NewChooseGroupActivity
import com.mai.nix.maiapp.helpers.UserSettings
import com.mai.nix.maiapp.helpers.UserSettings.setGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * Created by Nix on 03.08.2017.
 */

@ExperimentalCoroutinesApi
class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private lateinit var groupPreference: Preference
    private lateinit var chosenGroup: String

    companion object {
        private const val REQUEST_CODE_GROUP = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.backgroundSplash))
        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_prefs, rootKey)
        groupPreference = findPreference("pref_group")!!
        val clearSubjectsCache = findPreference<Preference>("clear_cache_subj")
        val clearExamsCache = findPreference<Preference>("clear_cache_ex")
        val frequencySubjects = findPreference<androidx.preference.ListPreference>("frequency_update_subjects")
        val frequencyExams = findPreference<androidx.preference.ListPreference>("frequency_update_exams")
        val theme = findPreference<androidx.preference.ListPreference>("pref_theme")
        val about = findPreference<Preference>("about")
        val mai = findPreference<Preference>("go_mai")

        groupPreference.summary = UserSettings.getGroup(requireContext())
        frequencySubjects?.value = UserSettings.getSubjectsUpdateFrequency(requireContext())
        frequencyExams?.value = UserSettings.getExamsUpdateFrequency(requireContext())

        groupPreference.onPreferenceClickListener = this
        clearSubjectsCache?.onPreferenceClickListener = this
        clearExamsCache?.onPreferenceClickListener = this
        about?.onPreferenceClickListener = this
        mai?.onPreferenceClickListener = this
        frequencySubjects?.onPreferenceChangeListener = this
        frequencyExams?.onPreferenceChangeListener = this
        theme?.onPreferenceChangeListener = this
    }

    private fun convertThemeValue(value: Any): Int {
        val i = value as String
        return i.toInt()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_GROUP && resultCode == Activity.RESULT_OK) {
            chosenGroup = data?.getStringExtra(NewChooseGroupActivity.EXTRA_GROUP)
                    ?: throw Exception("No group found")
            setGroup(requireContext(), chosenGroup)
            clearSubjectsCache(showMessage = false)
            clearExamsCache(showMessage = false)
            groupPreference.summary = chosenGroup
            requireActivity().setResult(Activity.RESULT_OK)
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            "pref_group" -> {
                val i = NewChooseGroupActivity.newIntent(requireContext(), true)
                startActivityForResult(i, REQUEST_CODE_GROUP)
            }
            "clear_cache_subj" -> {
                clearSubjectsCache(showMessage = true)
            }
            "clear_cache_ex" -> {
                clearExamsCache(showMessage = true)
            }
            "about" -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:novembernix@gmail.com?subject=Приложение МАИ")
                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), R.string.mail_error, Toast.LENGTH_SHORT).show()
                }
            }
            "go_mai" -> {
                val builder = CustomTabsIntent.Builder()
                builder.setShowTitle(true)
                builder.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.colorText))
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(requireContext(), Uri.parse("http://mai.ru/"))
            }
        }
        return true
    }

    private fun clearExamsCache(showMessage: Boolean) {
        lifecycleScope.launch {
            try {
                (requireContext().applicationContext as MaiApp).getDatabase().examDao.clearAll()
                if (showMessage) Toast.makeText(activity, R.string.clear_cache_exams_toast, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun clearSubjectsCache(showMessage: Boolean) {
        lifecycleScope.launch {
            try {
                (requireContext().applicationContext as MaiApp).getDatabase().scheduleDao.deleteAll()
                if (showMessage) Toast.makeText(activity, R.string.clear_cache_subj_toast, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            "frequency_update_subjects" -> {
                UserSettings.setSubjectsUpdateFrequency(requireContext(), newValue as String)
            }
            "frequency_update_exams" -> {
                UserSettings.setExamsUpdateFrequency(requireContext(), newValue as String)
            }
            "pref_theme" -> {
                val value = convertThemeValue(newValue)
                UserSettings.setTheme(requireContext(), value)
                AppCompatDelegate.setDefaultNightMode(value)
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }
}