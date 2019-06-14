package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.DoggoAdapter.ImageListAdapterListener
import com.tunjid.androidbootstrap.adapters.InputAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder
import com.tunjid.androidbootstrap.viewholders.InputViewHolder
import java.util.*

class AdoptDoggoFragment : AppBaseFragment(), ImageListAdapterListener {

    companion object {
        internal const val ARG_DOGGO = "doggo"

        fun newInstance(doggo: Doggo): AdoptDoggoFragment {
            val fragment = AdoptDoggoFragment()
            val args = Bundle()

            args.putParcelable(ARG_DOGGO, doggo)
            fragment.arguments = args
            fragment.prepareSharedElementTransition()

            return fragment
        }
    }

    private lateinit var doggo: Doggo

    override val fabState: FabExtensionAnimator.GlyphState
        get() = FabExtensionAnimator.newState(getString(R.string.adopt), ContextCompat.getDrawable(requireContext(), R.drawable.ic_hug_24dp))

    override val fabClickListener: View.OnClickListener
        get() = View.OnClickListener {
            showSnackbar { snackBar -> snackBar.setText(getString(R.string.adopted_doggo, doggo.name)) }
        }

    override fun getStableTag(): String {
        return super.getStableTag() + "-" + arguments!!.getParcelable<Parcelable>(ARG_DOGGO)!!.hashCode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doggo = arguments!!.getParcelable(ARG_DOGGO)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_adopt_doggo, container, false)

        ListManagerBuilder<InputViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.model_list))
                .withLinearLayoutManager()
                .withAdapter(InputAdapter(Arrays.asList(*resources.getStringArray(R.array.adoption_items))))
                .build()

        val viewHolder = DoggoViewHolder(root, this)
        viewHolder.bind(doggo)

        tintView(R.color.black_50, viewHolder.thumbnail, { color, imageView -> this.setColorFilter(color, imageView) })
        viewHolder.fullSize?.let { tintView(R.color.black_50, viewHolder.fullSize, { color, imageView -> this.setColorFilter(color, imageView) }) }

        return root
    }

    override fun showsToolBar(): Boolean = false

    override fun showsFab(): Boolean = true

    override fun insetFlags(): InsetFlags = InsetFlags.NO_TOP

    private fun setColorFilter(color: Int, imageView: ImageView) = imageView.setColorFilter(color)

    private fun prepareSharedElementTransition() {
        val baseSharedTransition = baseSharedTransition()

        sharedElementEnterTransition = baseSharedTransition
        sharedElementReturnTransition = baseSharedTransition
    }

}
