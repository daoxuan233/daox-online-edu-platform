package com.daox.online.entity.views.requestVO.teacher;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 课程视图 请求参数  类
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TeacherCourseVo implements Serializable {
    @Serial // 添加此字段，以解决序列化问题
    private static final long serialVersionUID = 1L;

    private String title;
    private String description;
    private String coverImageUrl;
    private String teacherId;
    private String categoryId;
    /**
     * 课程状态。
     * <p>
     * 该字段仅用于页面展示回传，普通更新接口不会直接据此修改课程状态。
     * </p>
     */
    private String status;
    private Integer enrollmentCount;
    private Integer isDeleted;
    private Integer isPrivate;

    @Override
    public String toString() {
        return "TeacherCourseVo{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", status='" + status + '\'' +
                ", enrollmentCount=" + enrollmentCount +
                ", isDeleted=" + isDeleted +
                ", isPrivate=" + isPrivate +
                '}';
    }
}
