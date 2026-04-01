package cn.edu.ccst.manpower_resource.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long current;
    private Long size;
    private Long total;
    private Long pages;
    private List<T> records;

    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.current = current;
        pageResult.size = size;
        pageResult.total = total;
        pageResult.pages = (total + size - 1) / size;
        pageResult.records = records;
        return pageResult;
    }
}
