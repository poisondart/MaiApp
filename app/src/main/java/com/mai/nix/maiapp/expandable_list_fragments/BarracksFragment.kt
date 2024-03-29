package com.mai.nix.maiapp.expandable_list_fragments

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.mai.nix.maiapp.R
import com.mai.nix.maiapp.navigation_fragments.campus.CampusAbstractFragment
import com.mai.nix.maiapp.navigation_fragments.campus.BarracksIntent
import kotlinx.android.synthetic.main.fragment_abstract_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Nix on 13.08.2017.
 */
@ExperimentalCoroutinesApi
class BarracksFragment : CampusAbstractFragment() {

    override fun initAdapter(): RecyclerView.Adapter<*> {
        return ExpandableListAdapter(arrayOf(R.drawable.ic_round_author_24, R.drawable.ic_round_contact_support_24))
    }

    override fun refresh() {
        lifecycleScope.launch {
            campusViewModel.barracksIntent.send(BarracksIntent.BarracksLoad)
        }
    }

    override fun setupViewModel() {
        lifecycleScope.launch {
            campusViewModel.barracksState.collect {
                abstractListSwipeRefreshLayout.isRefreshing = it.loading
                (adapter as ExpandableListAdapter).updateItems(it.items)
                adapter.notifyDataSetChanged()
                if (!it.error.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}