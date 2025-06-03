package com.actito.push.ui.notifications.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.actito.Actito
import com.actito.models.ActitoNotification
import com.actito.push.ui.databinding.ActitoNotificationImageFragmentBinding
import com.actito.push.ui.ktx.pushUIInternal
import com.actito.push.ui.notifications.fragments.base.NotificationFragment
import com.actito.utilities.image.loadImage
import com.actito.utilities.parcel.parcelable
import com.actito.utilities.threading.onMainThread

public class ActitoImageFragment : NotificationFragment() {

    private lateinit var binding: ActitoNotificationImageFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActitoNotificationImageFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            binding.pager.adapter = ImageAdapter(notification, this)
        }

        if (notification.content.isEmpty()) {
            onMainThread {
                Actito.pushUIInternal().lifecycleListeners.forEach {
                    it.get()?.onNotificationFailedToPresent(notification)
                }
            }
        } else {
            onMainThread {
                Actito.pushUIInternal().lifecycleListeners.forEach {
                    it.get()?.onNotificationPresented(notification)
                }
            }
        }
    }

    public class ImageAdapter(
        private val notification: ActitoNotification,
        fragment: Fragment,
    ) : FragmentStateAdapter(fragment) {

        override fun createFragment(position: Int): Fragment =
            ImageChildFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SAVED_STATE_CONTENT, notification.content[position])
                }
            }

        override fun getItemCount(): Int =
            notification.content.size
    }

    public class ImageChildFragment : Fragment() {
        private lateinit var content: ActitoNotification.Content

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            content = savedInstanceState?.parcelable(SAVED_STATE_CONTENT)
                ?: arguments?.parcelable(SAVED_STATE_CONTENT)
                ?: throw IllegalArgumentException("Missing required notification content parameter.")
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            ImageView(inflater.context)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            loadImage(requireContext(), content.data as String, view as ImageView)
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putParcelable(SAVED_STATE_CONTENT, content)
        }
    }

    public companion object {
        private const val SAVED_STATE_CONTENT = "com.actito.ui.Content"
    }
}
