package org.igye.sqlexercises.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.igye.sqlexercises.config.UserDetailsImpl;
import org.igye.sqlexercises.exceptions.AccessDeniedException;
import org.igye.sqlexercises.exceptions.OutlineException;
import org.igye.sqlexercises.htmlforms.CellType;
import org.igye.sqlexercises.htmlforms.IconInfo;
import org.igye.sqlexercises.model.Node;
import org.igye.sqlexercises.model.Paragraph;
import org.igye.sqlexercises.model.Topic;
import org.igye.sqlexercises.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutlineUtils {
    public static final String SQL_DEBUG_LOGGER_NAME = "sql-debug";
    public static final String NOTHING = "nothing";
    public static final String UUID_CHAR = "uuid-char";
    public static int BCRYPT_SALT_ROUNDS = 10;

    public static <T> T accessDenied() {
        throw new AccessDeniedException("Access denied.");
    }

    public static String hashPwd(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt(BCRYPT_SALT_ROUNDS));
    }

    public static boolean checkPwd(String pwd, String hashedPwd) {
        return BCrypt.checkpw(pwd, hashedPwd);
    }

    public static String prefix(String prefix, String url) {
        if (StringUtils.isEmpty(prefix)) {
            return url;
        } else {
            return "" + prefix + "/" + url;
        }
    }

    public static String redirect(String url) {
        return "redirect:/" + url;
    }

    public static String  redirect(HttpServletResponse response, String path, Map<String, Object> params) throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        params.forEach((k,v) -> builder.queryParam(k, v));
        response.sendRedirect("/" + path + "?" + builder.build().getQuery());
        return NOTHING;
    }

    public static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new OutlineException("obj == null");
        }
    }

    public static File getImgFile(String imagesLocation, UUID imgId) {
        String idStr = imgId.toString();
        return new File(imagesLocation + "/" + idStr.substring(0,2) + "/" + idStr);
    }

    public static <T> Optional<T> getNextSibling(List<T> list, Function<T,Boolean> comparator, boolean toTheRight) {
        if (CollectionUtils.isEmpty(list) ||
                !toTheRight && comparator.apply(list.get(0)) ||
                toTheRight && comparator.apply(list.get(list.size() - 1))) {
            return Optional.empty();
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (comparator.apply(list.get(i))) {
                    return Optional.of(list.get(i + (toTheRight ? 1 : -1)));
                }
            }
            throw new OutlineException("getNextSibling");
        }
    }

    public static <T> Optional<T> getFurthestSibling(List<T> list, Function<T,Boolean> comparator, Boolean toTheRight) {
        if (CollectionUtils.isEmpty(list) ||
                !toTheRight && comparator.apply(list.get(0)) ||
                toTheRight && comparator.apply(list.get(list.size() - 1))) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(toTheRight ? list.size() - 1 : 0));
        }
    }

    public static Session getCurrentSession(EntityManager entityManager) {
        return entityManager.unwrap(Session.class);
    }

    public static User getCurrentUser() {
        User user = ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        return User.builder()
                .id(user.getId())
                .name(user.getName())
                .roles(user.getRoles())
                .build()
                ;
    }

    public static <A,B> Set<B> map(Set<A> collection, Function<A,B> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <A,B> List<B> map(List<A> collection, Function<A,B> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    public static <A,B> List<B> map(A[] array, Function<A,B> mapper) {
        List<B> res = new ArrayList<>();
        for (A a : array) {
            res.add(mapper.apply(a));
        }
        return res;
    }

    public static <A,B> Set<B> mapToSet(List<A> collection, Function<A,B> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <T> Set<T> filter(Set<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(Collectors.toSet());
    }

    public static <T> List<T> filter(List<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(Collectors.toList());
    }

    public static <T> Set<T> filterToSet(List<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).collect(Collectors.toSet());
    }

    public static <E,K,V> Map<K,V> toMap(Collection<E> collection, Function<E,K> keyExtr, Function<E,V> valueExtr) {
        return collection.stream().collect(Collectors.toMap(keyExtr, valueExtr));
    }

    public static <E,K> Map<K,E> toMap(Collection<E> collection, Function<E,K> keyExtr) {
        return toMap(collection, keyExtr, Function.identity());
    }

    public static List<List<IconInfo>> getIconsInfo(List<Node> nodes) {
        List<List<IconInfo>> res1 = new ArrayList<>();
        res1.add(new LinkedList<>());
        for (Node node : nodes) {
            if (node instanceof Topic) {
                Topic topic = (Topic) node;
                if (topic.isSol() && !res1.get(res1.size() - 1).isEmpty()) {
                    res1.add(new LinkedList<>());
                }
                res1.get(res1.size() - 1).add(
                        IconInfo.builder()
                                .cellType(CellType.TOPIC)
                                .iconId(topic.getIcon() != null ? topic.getIcon().getId() : null)
                                .nodeId(node.getId())
                                .build()
                );
            } else {
                Paragraph par = (Paragraph) node;
                if (par.isSol() && !res1.get(res1.size() - 1).isEmpty()) {
                    res1.add(new LinkedList<>());
                }
                res1.get(res1.size() - 1).add(
                        IconInfo.builder()
                                .cellType(CellType.PARAGRAPH)
                                .iconId(par.getIcon() != null ? par.getIcon().getId() : null)
                                .nodeId(node.getId())
                                .build()
                );
            }
        }
        int maxSize = res1.stream().map(List::size).max(Integer::compareTo).get();
        for (List row : res1) {
            while (row.size() < maxSize) {
                row.add(IconInfo.builder().cellType(CellType.EMPTY).build());
            }
        }

        List<List<IconInfo>> res = new ArrayList<>();
        res.add(
                Stream.iterate(0, i -> i + 1).limit(res1.get(0).size() + 1).map(i ->
                        IconInfo.builder()
                                .cellType(i == 0 ? CellType.EMPTY : CellType.NUMBER)
                                .number(i)
                                .build()
                ).collect(Collectors.toList())
        );
        for (int i = 0; i < res1.size(); i++) {
            List<IconInfo> newRow = new LinkedList<>();
            newRow.add(
                    IconInfo.builder()
                            .cellType(CellType.NUMBER)
                            .number(i+1)
                            .build()
            );
            newRow.addAll(res1.get(i));
            res.add(newRow);
        }
        return res;
    }

    public static Map<String, Object> createResponse(
            String attrName1, Object value1,
            String attrName2, Object value2,
            String attrName3, Object value3,
            String attrName4, Object value4
    ) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "ok");
        resp.put(attrName1, value1);
        resp.put(attrName2, value2);
        resp.put(attrName3, value3);
        resp.put(attrName4, value4);
        return resp;
    }

    public static Map<String, Object> createResponse(
            String attrName1, Object value1,
            String attrName2, Object value2,
            String attrName3, Object value3
    ) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "ok");
        resp.put(attrName1, value1);
        resp.put(attrName2, value2);
        resp.put(attrName3, value3);
        return resp;
    }

    public static Map<String, Object> createResponse(
            String attrName1, Object value1,
            String attrName2, Object value2
    ) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "ok");
        resp.put(attrName1, value1);
        resp.put(attrName2, value2);
        return resp;
    }

    public static Map<String, Object> createResponse(String attrName, Object value) {
        Map<String, Object> resp = createVoidResponse();
        resp.put(attrName, value);
        return resp;
    }

    public static Map<String, Object> createVoidResponse() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "ok");
        return resp;
    }

    public static <E> List<E> listF(E... elems) {
        return ImmutableList.copyOf(elems);
    }

    public static <E> Set<E> setF(E... elems) {
        return ImmutableSet.copyOf(elems);
    }

    public static <K,V> Map<K,V> mapF(K k1, V v1) {
        Map<K, V> resp = new HashMap<>();
        resp.put(k1, v1);
        return resp;
    }

    public static <K,V> Map<K,V> mapF(K k1, V v1, K k2, V v2) {
        Map<K, V> resp = mapF(k2,v2);
        resp.put(k1, v1);
        return resp;
    }

    public static <K,V> Map<K,V> mapF(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> resp = mapF(k2,v2,k3,v3);
        resp.put(k1, v1);
        return resp;
    }
}
