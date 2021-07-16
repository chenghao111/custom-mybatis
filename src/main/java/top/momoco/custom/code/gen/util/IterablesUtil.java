package top.momoco.custom.code.gen.util;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 迭代 list  工具类 eg  IterablesUtil. forEach(Lists.newArrayList("1","2"),(x,y)->{})
 *
 * @author chenghao65
 * @date 2020/10/29
 * @since 1.0.0
 */
public class IterablesUtil {

    /**
     * 不允许收到创建静态工具类
     */
    private IterablesUtil() {
        throw new IllegalStateException("Utility class");
    }


    /***
     * 增加索引 的迭代 方式 
     * @param elements
     * @param action
     * @param <E>
     */
    public static <E> void forEach(
            Iterable<? extends E> elements, BiConsumer<Integer, ? super E> action) {
        Objects.requireNonNull(elements);
        Objects.requireNonNull(action);

        int index = 0;
        for (E element : elements) {
            action.accept(index++, element);
        }
    }
}
