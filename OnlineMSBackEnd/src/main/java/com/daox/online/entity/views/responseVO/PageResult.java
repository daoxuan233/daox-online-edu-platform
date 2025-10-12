package com.daox.online.entity.views.responseVO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 分页结果对象
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Integer current;
    private Integer size;
    private Integer pages;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public static <T> PageResult<T> of(List<T> records, Long total, Integer current, Integer size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages((int) Math.ceil((double) total / size));
        result.setHasNext(current < result.getPages());
        result.setHasPrevious(current > 1);
        return result;
    }
}