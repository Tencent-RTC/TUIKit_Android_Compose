package io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.pinyinhelper;

import java.util.Set;

public interface PinyinDict {
    Set<String> words();

    String[] toPinyin(String word);
}
