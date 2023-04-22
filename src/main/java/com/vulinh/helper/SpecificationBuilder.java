package com.vulinh.helper;

import java.util.Collection;
import javax.persistence.metamodel.SingularAttribute;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationBuilder {

  public static <E> Specification<E> always() {
    return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
  }

  public static <E> Specification<E> never() {
    return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
  }

  @SafeVarargs
  public static <E> Specification<E> and(
      Specification<E> firstSpecification, Specification<E>... otherSpecifications) {
    if (ArrayUtils.isEmpty(otherSpecifications)) {
      return firstSpecification;
    }

    var result = firstSpecification;

    for (var specification : otherSpecifications) {
      result = result.and(specification);
    }

    return result;
  }

  @SafeVarargs
  public static <E> Specification<E> or(
      Specification<E> firstSpecification, Specification<E>... otherSpecifications) {
    if (ArrayUtils.isEmpty(otherSpecifications)) {
      return firstSpecification;
    }

    var result = firstSpecification;

    for (var specification : otherSpecifications) {
      result = result.or(specification);
    }

    return result;
  }

  public static <E> Specification<E> not(Specification<E> specification) {
    return Specification.not(specification);
  }

  public static <E, F> Specification<E> eq(SingularAttribute<? super E, F> attribute, F value) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(attribute), value);
  }

  public static <E, F> Specification<E> neq(SingularAttribute<? super E, F> attribute, F value) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(attribute), value);
  }

  public static <E, F extends Comparable<? super F>> Specification<E> ge(
      SingularAttribute<? super E, F> attribute, F value) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThan(root.get(attribute), value);
  }

  public static <E, F extends Comparable<? super F>> Specification<E> geq(
      SingularAttribute<? super E, F> attribute, F value) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThanOrEqualTo(root.get(attribute), value);
  }

  public static <E, F extends Comparable<? super F>> Specification<E> le(
      SingularAttribute<? super E, F> attribute, F value) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(attribute), value);
  }

  public static <E, F extends Comparable<? super F>> Specification<E> leq(
      SingularAttribute<? super E, F> attribute, F value) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get(attribute), value);
  }

  public static <E, F extends Comparable<? super F>> Specification<E> between(
      SingularAttribute<? super E, F> attribute, F lowerBound, F upperBound) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.between(root.get(attribute), lowerBound, upperBound);
  }

  public static <E, F extends Comparable<? super F>> Specification<E> exclusiveBetween(
      SingularAttribute<? super E, F> attribute, F lowerBound, F upperBound) {
    return and(ge(attribute, lowerBound), le(attribute, upperBound));
  }

  public static <E, F extends Comparable<? super F>> Specification<E> lowerExclusiveBetween(
      SingularAttribute<? super E, F> attribute, F lowerBound, F upperBound) {
    return and(ge(attribute, lowerBound), leq(attribute, upperBound));
  }

  public static <E, F extends Comparable<? super F>> Specification<E> upperExclusiveBetween(
      SingularAttribute<? super E, F> attribute, F lowerBound, F upperBound) {
    return and(geq(attribute, lowerBound), le(attribute, upperBound));
  }

  public static <E, F> Specification<E> in(
      SingularAttribute<? super E, F> attribute, Collection<? extends F> collection) {
    return (root, query, criteriaBuilder) -> root.get(attribute).in(collection);
  }

  public static <E, F> Specification<E> notIn(
      SingularAttribute<? super E, F> attribute, Collection<? extends F> collection) {
    return not(in(attribute, collection));
  }

  public static <E> Specification<E> isNull(SingularAttribute<? super E, ?> attribute) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(attribute));
  }

  public static <E> Specification<E> notNull(SingularAttribute<? super E, ?> attribute) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(attribute));
  }

  public static <E> Specification<E> isTrue(SingularAttribute<? super E, Boolean> attribute) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get(attribute));
  }

  public static <E> Specification<E> isFalse(SingularAttribute<? super E, Boolean> attribute) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get(attribute));
  }

  @SuppressWarnings("unchecked")
  public static <E, F> Specification<E> like(
      SingularAttribute<? super E, F> attribute, String keyword) {
    var patternKeyword = createPatternKeyword(keyword);

    if (StringUtils.isBlank(patternKeyword)) {
      return always();
    }

    if (String.class.isAssignableFrom(attribute.getJavaType())) {
      return (root, query, criteriaBuilder) ->
          criteriaBuilder.like(
              criteriaBuilder.lower(root.get((SingularAttribute<E, String>) attribute)),
              patternKeyword);
    }

    return (root, query, criteriaBuilder) ->
        criteriaBuilder.like(
            criteriaBuilder.lower(root.get(attribute).as(String.class)), patternKeyword);
  }

  public static <E, F> Specification<E> notLike(
      SingularAttribute<? super E, F> attribute, String keyword) {
    return not(like(attribute, keyword));
  }

  private static String createPatternKeyword(String keyword) {
    if (StringUtils.isBlank(keyword)) {
      return StringUtils.EMPTY;
    }

    return '%' + keyword.toLowerCase() + '%';
  }
}
