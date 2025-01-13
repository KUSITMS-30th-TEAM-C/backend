package spring.backend.activity.infrastructure.persistence.jpa.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import spring.backend.activity.domain.value.Keyword;
import spring.backend.activity.dto.response.*;
import spring.backend.activity.dto.response.HomeActivityInfoResponse;
import spring.backend.activity.presentation.dto.response.UserMonthlyActivityDetail;
import spring.backend.activity.presentation.dto.response.UserMonthlyActivitySummary;
import spring.backend.activity.infrastructure.persistence.jpa.entity.ActivityJpaEntity;
import spring.backend.activity.query.dao.ActivityDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ActivityJpaDao extends JpaRepository<ActivityJpaEntity, Long>, ActivityDao {

    @Override
    @Query("""
        select new spring.backend.activity.dto.response.HomeActivityInfoResponse(
            a.id,
            a.keyword,
            a.title,
            a.savedTime
        )
        from ActivityJpaEntity a
        where a.memberId = :memberId
        and a.createdAt between :startDateTime and :endDateTime
        and a.finished = true
        order by a.createdAt ASC
    """)
    List<HomeActivityInfoResponse> findTodayActivities(UUID memberId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Override
    @Query("""
                select new spring.backend.activity.dto.response.MonthlySavedTimeAndActivityCountResponse(
                coalesce(sum(a.savedTime), 0),
                coalesce(count(a), 0)
                )
                from ActivityJpaEntity a
                where a.memberId = :memberId
                and a.createdAt between :startDateTime and :endDateTime
                and a.finished = true
    """)
    MonthlySavedTimeAndActivityCountResponse findMonthlyTotalSavedTimeAndTotalCount(UUID memberId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Override
    @Query("""
                select new spring.backend.activity.dto.response.MonthlyActivityCountByKeywordResponse(
                    a.keyword,
                    coalesce(count(a), 0)
                )
                from ActivityJpaEntity a
                where a.memberId = :memberId
                and a.createdAt between :startDateTime and :endDateTime
                and a.finished = true
                group by a.keyword
                order by count (a) desc
    """)
    List<MonthlyActivityCountByKeywordResponse> findMonthlyActivitiesByKeywordSummary(UUID memberId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Override
    @Query("""
        select new spring.backend.activity.presentation.dto.response.UserMonthlyActivitySummary(
            m.createdAt,
            coalesce(sum(a.savedTime), 0),
            count(a)
        )
        from MemberJpaEntity m
        left join ActivityJpaEntity a on a.memberId = m.id
            and a.finished = true
            and function('year', a.createdAt) = :year
            and function('month', a.createdAt) = :month
        where m.id = :memberId
    """)
    UserMonthlyActivitySummary findActivitySummaryByYearAndMonth(UUID memberId, int year, int month);


    @Override
    @Query("""
        select new spring.backend.activity.presentation.dto.response.UserMonthlyActivityDetail(
            a.keyword.category,
            a.title,
            a.savedTime,
            a.createdAt
        )
        from ActivityJpaEntity a
        where a.memberId = :memberId
        and a.finished = true
        and function('year', a.createdAt) = :year
        and function('month', a.createdAt) = :month
        order by a.createdAt desc
    """)
    List<UserMonthlyActivityDetail> findActivityDetailsByYearAndMonth(UUID memberId, int year, int month);

    @Override
    @Query("""
                select new spring.backend.activity.dto.response.ActivityWithTitleAndSavedTimeResponse(
                    a.title,
                    coalesce(sum(a.savedTime), 0),
                    a.createdAt
                )
                from ActivityJpaEntity a
                where a.memberId = :memberId
                and a.createdAt between :startDateTime and :endDateTime
                and a.finished = true
                and a.keyword.category = :keywordCategory
                order by a.createdAt DESC
    """)
    List<ActivityWithTitleAndSavedTimeResponse> findActivitiesByMemberAndKeywordInMonth(UUID memberId, LocalDateTime startDateTime, LocalDateTime endDateTime, Keyword.Category keywordCategory);

    @Override
    @Query("""
                select new spring.backend.activity.dto.response.TotalSavedTimeAndActivityCountByKeywordInMonth(
                    coalesce(sum(a.savedTime), 0),
                    coalesce(count(a), 0)
                )
                from ActivityJpaEntity a
                where a.memberId = :memberId
                and a.createdAt between :startDateTime and :endDateTime
                and a.finished = true
                and a.keyword.category = :keywordCategory
    """)
    TotalSavedTimeAndActivityCountByKeywordInMonth findTotalSavedTimeAndActivityCountByKeywordInMonth(UUID memberId, LocalDateTime startDateTime, LocalDateTime endDateTime, Keyword.Category keywordCategory);

}
