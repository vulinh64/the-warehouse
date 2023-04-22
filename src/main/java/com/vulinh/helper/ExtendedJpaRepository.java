package com.vulinh.helper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExtendedJpaRepository<T, I>
    extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {

  default T findByIdOrFail(I id) {
    return findById(id).orElseThrow();
  }

  default T findByIdOrNull(I id) {
    return findById(id).orElse(null);
  }
}
