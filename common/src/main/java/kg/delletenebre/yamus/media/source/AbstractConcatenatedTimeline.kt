package kg.delletenebre.yamus.media.source

import android.util.Pair
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.ShuffleOrder

/**
 * Abstract base class for the concatenation of one or more [Timeline]s.
 */
/* package */ internal abstract class AbstractConcatenatedTimeline
/**
 * Sets up a concatenated timeline with a shuffle order of child timelines.
 *
 * @param isAtomic Whether the child timelines shall be treated as atomic, i.e., treated as a
 * single item for repeating and shuffling.
 * @param shuffleOrder A shuffle order of child timelines. The number of child timelines must
 * match the number of elements in the shuffle order.
 */
(private val isAtomic: Boolean, private val shuffleOrder: ShuffleOrder) : Timeline() {

    private val childCount: Int

    init {
        this.childCount = shuffleOrder.length
    }

    override fun getNextWindowIndex(windowIndex: Int, @Player.RepeatMode repeatMode: Int,
                                    shuffleModeEnabled: Boolean): Int {
        var repeatMode = repeatMode
        var shuffleModeEnabled = shuffleModeEnabled
        if (isAtomic) {
            // Adapt repeat and shuffle mode to atomic concatenation.
            repeatMode = if (repeatMode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_ALL else repeatMode
            shuffleModeEnabled = false
        }
        // Find next window within current child.
        val childIndex = getChildIndexByWindowIndex(windowIndex)
        val firstWindowIndexInChild = getFirstWindowIndexByChildIndex(childIndex)
        val nextWindowIndexInChild = getTimelineByChildIndex(childIndex).getNextWindowIndex(
                windowIndex - firstWindowIndexInChild,
                if (repeatMode == Player.REPEAT_MODE_ALL) Player.REPEAT_MODE_OFF else repeatMode,
                shuffleModeEnabled)
        if (nextWindowIndexInChild != C.INDEX_UNSET) {
            return firstWindowIndexInChild + nextWindowIndexInChild
        }
        // If not found, find first window of next non-empty child.
        var nextChildIndex = getNextChildIndex(childIndex, shuffleModeEnabled)
        while (nextChildIndex != C.INDEX_UNSET && getTimelineByChildIndex(nextChildIndex).isEmpty) {
            nextChildIndex = getNextChildIndex(nextChildIndex, shuffleModeEnabled)
        }
        if (nextChildIndex != C.INDEX_UNSET) {
            return getFirstWindowIndexByChildIndex(nextChildIndex) + getTimelineByChildIndex(nextChildIndex).getFirstWindowIndex(shuffleModeEnabled)
        }
        // If not found, this is the last window.
        return if (repeatMode == Player.REPEAT_MODE_ALL) {
            getFirstWindowIndex(shuffleModeEnabled)
        } else C.INDEX_UNSET
    }

    override fun getPreviousWindowIndex(windowIndex: Int, @Player.RepeatMode repeatMode: Int,
                                        shuffleModeEnabled: Boolean): Int {
        var repeatMode = repeatMode
        var shuffleModeEnabled = shuffleModeEnabled
        if (isAtomic) {
            // Adapt repeat and shuffle mode to atomic concatenation.
            repeatMode = if (repeatMode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_ALL else repeatMode
            shuffleModeEnabled = false
        }
        // Find previous window within current child.
        val childIndex = getChildIndexByWindowIndex(windowIndex)
        val firstWindowIndexInChild = getFirstWindowIndexByChildIndex(childIndex)
        val previousWindowIndexInChild = getTimelineByChildIndex(childIndex).getPreviousWindowIndex(
                windowIndex - firstWindowIndexInChild,
                if (repeatMode == Player.REPEAT_MODE_ALL) Player.REPEAT_MODE_OFF else repeatMode,
                shuffleModeEnabled)
        if (previousWindowIndexInChild != C.INDEX_UNSET) {
            return firstWindowIndexInChild + previousWindowIndexInChild
        }
        // If not found, find last window of previous non-empty child.
        var previousChildIndex = getPreviousChildIndex(childIndex, shuffleModeEnabled)
        while (previousChildIndex != C.INDEX_UNSET && getTimelineByChildIndex(previousChildIndex).isEmpty) {
            previousChildIndex = getPreviousChildIndex(previousChildIndex, shuffleModeEnabled)
        }
        if (previousChildIndex != C.INDEX_UNSET) {
            return getFirstWindowIndexByChildIndex(previousChildIndex) + getTimelineByChildIndex(previousChildIndex).getLastWindowIndex(shuffleModeEnabled)
        }
        // If not found, this is the first window.
        return if (repeatMode == Player.REPEAT_MODE_ALL) {
            getLastWindowIndex(shuffleModeEnabled)
        } else C.INDEX_UNSET
    }

    override fun getLastWindowIndex(shuffleModeEnabled: Boolean): Int {
        var shuffleModeEnabled = shuffleModeEnabled
        if (childCount == 0) {
            return C.INDEX_UNSET
        }
        if (isAtomic) {
            shuffleModeEnabled = false
        }
        // Find last non-empty child.
        var lastChildIndex = if (shuffleModeEnabled) shuffleOrder.lastIndex else childCount - 1
        while (getTimelineByChildIndex(lastChildIndex).isEmpty) {
            lastChildIndex = getPreviousChildIndex(lastChildIndex, shuffleModeEnabled)
            if (lastChildIndex == C.INDEX_UNSET) {
                // All children are empty.
                return C.INDEX_UNSET
            }
        }
        return getFirstWindowIndexByChildIndex(lastChildIndex) + getTimelineByChildIndex(lastChildIndex).getLastWindowIndex(shuffleModeEnabled)
    }

    override fun getFirstWindowIndex(shuffleModeEnabled: Boolean): Int {
        var shuffleModeEnabled = shuffleModeEnabled
        if (childCount == 0) {
            return C.INDEX_UNSET
        }
        if (isAtomic) {
            shuffleModeEnabled = false
        }
        // Find first non-empty child.
        var firstChildIndex = if (shuffleModeEnabled) shuffleOrder.firstIndex else 0
        while (getTimelineByChildIndex(firstChildIndex).isEmpty) {
            firstChildIndex = getNextChildIndex(firstChildIndex, shuffleModeEnabled)
            if (firstChildIndex == C.INDEX_UNSET) {
                // All children are empty.
                return C.INDEX_UNSET
            }
        }
        return getFirstWindowIndexByChildIndex(firstChildIndex) + getTimelineByChildIndex(firstChildIndex).getFirstWindowIndex(shuffleModeEnabled)
    }

    override fun getWindow(
            windowIndex: Int, window: Timeline.Window, setTag: Boolean, defaultPositionProjectionUs: Long): Timeline.Window {
        val childIndex = getChildIndexByWindowIndex(windowIndex)
        val firstWindowIndexInChild = getFirstWindowIndexByChildIndex(childIndex)
        val firstPeriodIndexInChild = getFirstPeriodIndexByChildIndex(childIndex)
        getTimelineByChildIndex(childIndex)
                .getWindow(
                        windowIndex - firstWindowIndexInChild, window, setTag, defaultPositionProjectionUs)
        window.firstPeriodIndex += firstPeriodIndexInChild
        window.lastPeriodIndex += firstPeriodIndexInChild
        return window
    }

    override fun getPeriodByUid(uid: Any, period: Timeline.Period): Timeline.Period {
        val childUid = getChildTimelineUidFromConcatenatedUid(uid)
        val periodUid = getChildPeriodUidFromConcatenatedUid(uid)
        val childIndex = getChildIndexByChildUid(childUid)
        val firstWindowIndexInChild = getFirstWindowIndexByChildIndex(childIndex)
        getTimelineByChildIndex(childIndex).getPeriodByUid(periodUid, period)
        period.windowIndex += firstWindowIndexInChild
        period.uid = uid
        return period
    }

    override fun getPeriod(periodIndex: Int, period: Timeline.Period, setIds: Boolean): Timeline.Period {
        val childIndex = getChildIndexByPeriodIndex(periodIndex)
        val firstWindowIndexInChild = getFirstWindowIndexByChildIndex(childIndex)
        val firstPeriodIndexInChild = getFirstPeriodIndexByChildIndex(childIndex)
        getTimelineByChildIndex(childIndex).getPeriod(periodIndex - firstPeriodIndexInChild, period,
                setIds)
        period.windowIndex += firstWindowIndexInChild
        if (setIds) {
            period.uid = getConcatenatedUid(getChildUidByChildIndex(childIndex), period.uid)
        }
        return period
    }

    override fun getIndexOfPeriod(uid: Any): Int {
        if (uid !is Pair<*, *>) {
            return C.INDEX_UNSET
        }
        val childUid = getChildTimelineUidFromConcatenatedUid(uid)
        val periodUid = getChildPeriodUidFromConcatenatedUid(uid)
        val childIndex = getChildIndexByChildUid(childUid)
        if (childIndex == C.INDEX_UNSET) {
            return C.INDEX_UNSET
        }
        val periodIndexInChild = getTimelineByChildIndex(childIndex).getIndexOfPeriod(periodUid)
        return if (periodIndexInChild == C.INDEX_UNSET)
            C.INDEX_UNSET
        else
            getFirstPeriodIndexByChildIndex(childIndex) + periodIndexInChild
    }

    override fun getUidOfPeriod(periodIndex: Int): Any {
        val childIndex = getChildIndexByPeriodIndex(periodIndex)
        val firstPeriodIndexInChild = getFirstPeriodIndexByChildIndex(childIndex)
        val periodUidInChild = getTimelineByChildIndex(childIndex).getUidOfPeriod(periodIndex - firstPeriodIndexInChild)
        return getConcatenatedUid(getChildUidByChildIndex(childIndex), periodUidInChild)
    }

    /**
     * Returns the index of the child timeline containing the given period index.
     *
     * @param periodIndex A valid period index within the bounds of the timeline.
     */
    protected abstract fun getChildIndexByPeriodIndex(periodIndex: Int): Int

    /**
     * Returns the index of the child timeline containing the given window index.
     *
     * @param windowIndex A valid window index within the bounds of the timeline.
     */
    protected abstract fun getChildIndexByWindowIndex(windowIndex: Int): Int

    /**
     * Returns the index of the child timeline with the given UID or [C.INDEX_UNSET] if not
     * found.
     *
     * @param childUid A child UID.
     * @return Index of child timeline or [C.INDEX_UNSET] if UID was not found.
     */
    protected abstract fun getChildIndexByChildUid(childUid: Any): Int

    /**
     * Returns the child timeline for the child with the given index.
     *
     * @param childIndex A valid child index within the bounds of the timeline.
     */
    protected abstract fun getTimelineByChildIndex(childIndex: Int): Timeline

    /**
     * Returns the first period index belonging to the child timeline with the given index.
     *
     * @param childIndex A valid child index within the bounds of the timeline.
     */
    protected abstract fun getFirstPeriodIndexByChildIndex(childIndex: Int): Int

    /**
     * Returns the first window index belonging to the child timeline with the given index.
     *
     * @param childIndex A valid child index within the bounds of the timeline.
     */
    protected abstract fun getFirstWindowIndexByChildIndex(childIndex: Int): Int

    /**
     * Returns the UID of the child timeline with the given index.
     *
     * @param childIndex A valid child index within the bounds of the timeline.
     */
    protected abstract fun getChildUidByChildIndex(childIndex: Int): Any

    private fun getNextChildIndex(childIndex: Int, shuffleModeEnabled: Boolean): Int {
        return if (shuffleModeEnabled)
            shuffleOrder.getNextIndex(childIndex)
        else if (childIndex < childCount - 1) childIndex + 1 else C.INDEX_UNSET
    }

    private fun getPreviousChildIndex(childIndex: Int, shuffleModeEnabled: Boolean): Int {
        return if (shuffleModeEnabled)
            shuffleOrder.getPreviousIndex(childIndex)
        else if (childIndex > 0) childIndex - 1 else C.INDEX_UNSET
    }

    companion object {

        /**
         * Returns UID of child timeline from a concatenated period UID.
         *
         * @param concatenatedUid UID of a period in a concatenated timeline.
         * @return UID of the child timeline this period belongs to.
         */
        fun getChildTimelineUidFromConcatenatedUid(concatenatedUid: Any): Any {
            return (concatenatedUid as Pair<*, *>).first
        }

        /**
         * Returns UID of the period in the child timeline from a concatenated period UID.
         *
         * @param concatenatedUid UID of a period in a concatenated timeline.
         * @return UID of the period in the child timeline.
         */
        fun getChildPeriodUidFromConcatenatedUid(concatenatedUid: Any): Any {
            return (concatenatedUid as Pair<*, *>).second
        }

        /**
         * Returns concatenated UID for a period in a child timeline.
         *
         * @param childTimelineUid UID of the child timeline this period belongs to.
         * @param childPeriodUid UID of the period in the child timeline.
         * @return UID of the period in the concatenated timeline.
         */
        fun getConcatenatedUid(childTimelineUid: Any, childPeriodUid: Any?): Any {
            return Pair.create<Any, Any>(childTimelineUid, childPeriodUid)
        }
    }

}