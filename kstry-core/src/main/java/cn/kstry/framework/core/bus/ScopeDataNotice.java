/*
 *
 *  * Copyright (c) 2020-2024, Lykan (jiashuomeng@gmail.com).
 *  * <p>
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * <p>
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package cn.kstry.framework.core.bus;

import cn.kstry.framework.core.exception.ExceptionEnum;
import cn.kstry.framework.core.util.AssertUtil;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 可作为服务节点方法返回值，用来将需要的变量通知到 StoryBus
 *
 * @author lykan
 */
public class ScopeDataNotice extends HashMap<String, Object> {

    /**
     * 变量通知至sta域
     */
    private final boolean noticeStaScope;

    /**
     * 变量通知至var域
     */
    private final boolean noticeVarScope;

    /**
     * 通知res域
     */
    private Object result;

    private final Set<String> excludeKeyVarSet;

    private final Set<String> excludeKeyStaSet;

    private ScopeDataNotice(boolean noticeStaScope, boolean noticeVarScope) {
        this.noticeStaScope = noticeStaScope;
        this.noticeVarScope = noticeVarScope;
        this.excludeKeyVarSet = Sets.newHashSet();
        this.excludeKeyStaSet = Sets.newHashSet();
    }

    public boolean isNoticeStaScope() {
        return noticeStaScope;
    }

    public boolean isNoticeVarScope() {
        return noticeVarScope;
    }

    public Object getResult() {
        return result;
    }

    public Set<String> getExcludeKeyVarSet() {
        return excludeKeyVarSet;
    }

    public Set<String> getExcludeKeyStaSet() {
        return excludeKeyStaSet;
    }

    public ScopeDataNotice notice(String key, Object value) {
        if (StringUtils.isBlank(key)) {
            return this;
        }
        AssertUtil.isTrue(noticeVarScope || noticeStaScope, ExceptionEnum.COMPONENT_PARAMS_ERROR, "notice invalid. key: {}", key);
        put(key, value);
        return this;
    }

    public ScopeDataNotice noticeVar(String key, Object value) {
        AssertUtil.isTrue(noticeVarScope, ExceptionEnum.COMPONENT_PARAMS_ERROR, "noticeVar invalid. key: {}", key);
        excludeKeyStaSet.add(key);
        return notice(key, value);
    }

    public ScopeDataNotice noticeSta(String key, Object value) {
        AssertUtil.isTrue(noticeStaScope, ExceptionEnum.COMPONENT_PARAMS_ERROR, "noticeSta invalid. key: {}", key);
        excludeKeyVarSet.add(key);
        return notice(key, value);
    }

    public ScopeDataNotice notice(List<String> keys, Object value) {
        if (CollectionUtils.isEmpty(keys)) {
            return this;
        }
        AssertUtil.isTrue(noticeVarScope || noticeStaScope, ExceptionEnum.COMPONENT_PARAMS_ERROR, "notice invalid. keys: {}", keys);
        String key = keys.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("."));
        return notice(key, value);
    }

    public ScopeDataNotice result(Object result) {
        this.result = result;
        return this;
    }

    public static ScopeDataNotice res(Object result) {
        ScopeDataNotice scopeDataNotice = new ScopeDataNotice(false, false);
        return scopeDataNotice.result(result);
    }

    public static ScopeDataNotice sta() {
        return new ScopeDataNotice(true, false);
    }

    public static ScopeDataNotice var() {
        return new ScopeDataNotice(false, true);
    }

    public static ScopeDataNotice both() {
        return new ScopeDataNotice(true, true);
    }
}
