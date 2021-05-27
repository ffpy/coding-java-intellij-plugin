package org.ffpy.plugin.coding.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 注释位置
 */
@Getter
@RequiredArgsConstructor
public enum CommentPosition {
    UP("节点上方", e -> e.selectNodes("./preceding-sibling::comment()[1]")),
    DOWN("节点下方", e -> e.selectNodes("./following-sibling::comment()[1]")),
    NONE("无", e -> Collections.emptyList()),
    ;
    private final String name;
    private final Function<Element, List<Node>> finder;
}
