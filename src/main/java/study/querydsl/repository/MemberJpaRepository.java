package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member){
        em.persist(member);
    }


    public Optional<Member> findById(Long id){
        Member findMember = em.find(Member.class, id);

        // 해당 객체가 null인지 아닌지 자신없는 상황에서 사용하는 Optional 메소드입니다.
        return Optional.ofNullable(findMember);
    }


    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username){
        return queryFactory.selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<Member> findAll_Querydsl(){
        return queryFactory.selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username){

        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username).getResultList();

    }


    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){

        BooleanBuilder builder = new BooleanBuilder();

        // web에서는 parameter로 "" 와 같은 빈 문자열이 자주 들어오기 때문에 스프링에서 제공해주는 StringUtils 클래스를 사용하면 유용합니다.
        if (StringUtils.hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }

        if (StringUtils.hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if (condition.getAgeGoe() != null) {
           builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }


        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();


    }


}
