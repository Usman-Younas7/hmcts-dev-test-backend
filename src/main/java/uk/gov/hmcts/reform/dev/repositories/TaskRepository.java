package uk.gov.hmcts.reform.dev.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByDueDateTimeAsc();

    List<Task> findAllByStatusOrderByDueDateTimeAsc(TaskStatus status);
}
