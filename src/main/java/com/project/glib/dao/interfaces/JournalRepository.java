package com.project.glib.dao.interfaces;

import com.project.glib.model.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource()
public interface JournalRepository extends JpaRepository<Journal, Long> {
    boolean existsAllByTitle(String title);
}
