package com.android.QKSMS.interactor

import com.android.QKSMS.manager.ShortcutManager
import com.android.QKSMS.repository.ConversationRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkUnLocked @Inject constructor(
        private val conversationRepo: ConversationRepository,
        private val updateBadge: UpdateBadge,
        private val shortcutManager: ShortcutManager
) : Interactor<List<Long>>() {

    override fun buildObservable(params: List<Long>): Flowable<*> {
        return Flowable.just(params.toLongArray())
                .doOnNext { threadIds -> conversationRepo.markUnLocked(*threadIds) }
                .doOnNext { shortcutManager.updateShortcuts() } // Update shortcuts
                .flatMap { updateBadge.buildObservable(Unit) } // Update the badge and widget
    }

}