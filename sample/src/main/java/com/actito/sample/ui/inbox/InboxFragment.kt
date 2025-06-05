package com.actito.sample.ui.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.actito.Actito
import com.actito.inbox.ktx.inbox
import com.actito.inbox.models.ActitoInboxItem
import com.actito.sample.R
import com.actito.sample.core.BaseFragment
import com.actito.sample.databinding.FragmentInboxBinding

class InboxFragment : BaseFragment() {
    private lateinit var binding: FragmentInboxBinding
    private val viewModel: InboxViewModel by viewModels()
    private val adapter = InboxAdapter(::onInboxItemClicked, ::onInboxItemLongPressed)

    override val baseViewModel: InboxViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentInboxBinding.inflate(inflater, container, false)

        Actito.inbox().observableBadge.observe(requireActivity()) { badge ->
            baseViewModel.showSnackBar("Unread count: $badge")

            if (badge != Actito.inbox().badge) {
                AlertDialog.Builder(requireContext())
                    .setMessage(
                        "Badge mismatch.\nLiveData = $badge\nActitoInbox.badge = ${Actito.inbox().badge}",
                    )
                    .show()
            }
        }

        setupList()
        setupObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onPrepareMenu(menu: Menu) {
                    // Handle for example visibility of menu items
                }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.inbox, menu)
                }

                override fun onMenuItemSelected(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.refresh -> onRefreshClicked()
                        R.id.read_all -> onReadAllClicked()
                        R.id.remove_all -> onRemoveAllClicked()
                    }

                    return true
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )
    }

    private fun setupList() {
        binding.inboxList.layoutManager = LinearLayoutManager(requireContext())
        binding.inboxList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.inboxList.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun onInboxItemClicked(item: ActitoInboxItem) {
        viewModel.open(requireActivity(), item)
    }

    private fun onInboxItemLongPressed(item: ActitoInboxItem) {
        InboxItemActionsBottomSheet(
            onOpenClicked = { onInboxItemClicked(item) },
            onMarkAsReadClicked = { onMarkItemAsReadClicked(item) },
            onRemoveClicked = { onRemoveItemClicked(item) },
        ).show(childFragmentManager, "options-bottom-sheet")
    }

    private fun onMarkItemAsReadClicked(item: ActitoInboxItem) {
        viewModel.markAsRead(item)
    }

    private fun onRemoveItemClicked(item: ActitoInboxItem) {
        viewModel.remove(item)
    }

    private fun onReadAllClicked() {
        viewModel.markAllAsRead()
    }

    private fun onRemoveAllClicked() {
        viewModel.clearInbox()
    }

    private fun onRefreshClicked() {
        viewModel.refresh()
    }
}
