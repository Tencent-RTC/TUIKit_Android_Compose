package io.trtc.tuikit.atomicx.basecomponent.basiccontrols.azorderedlist.pinyinhelper;

import java.util.Map;
import java.util.Set;

public abstract class PinyinMapDict implements PinyinDict {
    public abstract Map<String, String[]> mapping();

    @Override
    public Set<String> words() {
        return mapping() != null ? mapping().keySet() : null;
    }

    @Override
    public String[] toPinyin(String word) {
        return mapping() != null ? mapping().get(word) : null;
    }
}
