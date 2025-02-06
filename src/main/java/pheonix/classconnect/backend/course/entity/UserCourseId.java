package pheonix.classconnect.backend.course.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class UserCourseId implements Serializable {
    private Long userId;
    private Long courseId;

    @Override
    public int hashCode() {
        return (userId == null ? 0 : userId.hashCode()) ^ (courseId == null ? 0 : courseId.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCourseId that = (UserCourseId) o;
        return userId.equals(that.userId) && courseId.equals(that.courseId);
    }
}
