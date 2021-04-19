package com.tunjid.androidx.tablists.doggo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.R
import com.tunjid.androidx.recyclerview.diff.Diffable
import com.tunjid.androidx.toLiveData
import io.reactivex.processors.PublishProcessor
import java.util.*

sealed class RankAction {
    sealed class Edit : RankAction() {
        data class Swap(val from: Doggo, val to: Doggo) : Edit()
        data class Remove(val doggo: Doggo) : Edit()
    }

    data class SwipeDragStarted(val doggo: Doggo, val actionState: Int) : RankAction()
    data class SwipeDragEnded(val doggo: Doggo, val actionState: Int) : RankAction()
    data class ViewHash(val hashCode: Int) : RankAction()
    object Reset : RankAction()
}

data class IndexedDoggo(
    val index: Int,
    val doggo: Doggo
) : Diffable {
    override val diffId: String
        get() = doggo.diffId
}

data class RankState(
    val viewHash: Int = 0,
    val doggos: List<IndexedDoggo> = Doggo.doggos.mapIndexed(::IndexedDoggo),
    val startUUID: UUID = UUID.randomUUID(),
    val editWithUUID: Pair<RankAction.Edit, UUID>? = null,
    val text: String? = null
)

class DoggoRankViewModel(application: Application) : AndroidViewModel(application) {

    private val processor: PublishProcessor<RankAction> = PublishProcessor.create()

    val state = processor.scan(RankState(), ::reduce).toLiveData()

    fun accept(action: RankAction) = processor.onNext(action)

    private fun reduce(state: RankState, action: RankAction): RankState = when (action) {
        is RankAction.Edit.Swap -> state.copy(
            editWithUUID = action to state.startUUID,
            doggos = state.doggos.toMutableList().let { mutableList ->
                val fromIndex = mutableList.indexOfFirst { it.doggo == action.from }
                val toIndex = mutableList.indexOfFirst { it.doggo == action.to }

                if (fromIndex > -1 && toIndex > -1) mutableList[fromIndex] = mutableList.set(toIndex, mutableList[fromIndex])
                mutableList
            }
        )
        is RankAction.Edit.Remove -> state.copy(
            editWithUUID = action to state.startUUID,
            doggos = state.doggos
                .map(IndexedDoggo::doggo)
                .filterNot(action.doggo::equals)
                .mapIndexed(::IndexedDoggo)
        )
        is RankAction.SwipeDragStarted -> state.copy(
            startUUID = UUID.randomUUID()
        )
        is RankAction.SwipeDragEnded -> state.copy(
            editWithUUID = null,
            doggos = state.doggos.mapIndexed { index, indexedDoggo -> indexedDoggo.copy(index = index) },
            text = when (val editWithUUID = state.editWithUUID) {
                null -> null
                else -> when (editWithUUID.second) {
                    state.startUUID -> when (val edit = editWithUUID.first) {
                        is RankAction.Edit.Swap -> getApplication<Application>().getString(
                            R.string.doggo_moved,
                            edit.from.name,
                            state.doggos.map(IndexedDoggo::doggo).indexOf(edit.from) + 1
                        )
                        is RankAction.Edit.Remove -> getApplication<Application>().getString(R.string.doggo_removed, edit.doggo.name)
                    }
                    else -> null
                }
            }
        )
        is RankAction.ViewHash -> state.copy(viewHash = action.hashCode)
        RankAction.Reset -> RankState()
    }
}
