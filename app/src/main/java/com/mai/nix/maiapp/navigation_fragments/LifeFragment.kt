package com.mai.nix.maiapp.navigation_fragments

import androidx.viewpager.widget.ViewPager
import com.mai.nix.maiapp.TabsFragment
import com.mai.nix.maiapp.ViewPagerAdapter
import com.mai.nix.maiapp.expandable_list_fragments.SportSectionsFragment
import com.mai.nix.maiapp.simple_list_fragments.AssociationsFragment
import com.mai.nix.maiapp.simple_list_fragments.StudentOrganisationsFragment

/**
 * Created by Nix on 01.08.2017.
 */
class LifeFragment : TabsFragment() {
    override fun setupViewPager(viewPager: ViewPager?) {
        setHasOptionsMenu(false)
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(StudentOrganisationsFragment(), "Студенческие объединения")
        adapter.addFragment(SportSectionsFragment(), "Спортивные секции")
        adapter.addFragment(AssociationsFragment(), "Объединения сотрудников и выпускников")
        viewPager?.adapter = adapter
    }
}