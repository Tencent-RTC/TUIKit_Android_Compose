package io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.pinyinhelper;

import org.ahocorasick.trie.Emit;

import java.util.Collection;
import java.util.List;

interface SegmentationSelector {
    List<Emit> select(Collection<Emit> emits);
}
