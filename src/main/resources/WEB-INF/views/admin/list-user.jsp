<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý Tài khoản - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261128-v1">
</head>
<body class="admin-page">
<div class="admin-shell">
    <jsp:include page="/WEB-INF/commons/admin/left.jsp" />

    <div class="admin-workspace">

        <main class="admin-main">
            <div class="admin-stats">
                <div class="admin-stat">
                    <div class="admin-stat-label">Tổng tài khoản</div>
                    <div class="admin-stat-value">${totalUsers}</div>
                </div>
                <div class="admin-stat">
                    <div class="admin-stat-label">Đã kích hoạt</div>
                    <div class="admin-stat-value">${activeUsers}</div>
                </div>
                <div class="admin-stat">
                    <div class="admin-stat-label">Quản trị viên (Admin)</div>
                    <div class="admin-stat-value">${adminUsers}</div>
                </div>
            </div>

            <c:if test="${not empty userError}">
                <div class="admin-alert"><c:out value="${userError}" /></div>
            </c:if>
            <c:if test="${param.userAdded == '1'}">
                <div class="admin-alert admin-alert-success">Đã thêm tài khoản mới thành công.</div>
            </c:if>
            <c:if test="${param.userUpdated == '1'}">
                <div class="admin-alert admin-alert-success">Đã cập nhật thông tin tài khoản thành công.</div>
            </c:if>
            <c:if test="${param.userDeleted == '1'}">
                <div class="admin-alert admin-alert-success">Đã xóa tài khoản.</div>
            </c:if>
            <c:if test="${param.userError == 'self'}">
                <div class="admin-alert">Không thể xóa tài khoản đang đăng nhập.</div>
            </c:if>
            <c:if test="${param.userError == 'missing'}">
                <div class="admin-alert">Không tìm thấy tài khoản cần thao tác.</div>
            </c:if>
            <c:if test="${param.userError == 'delete'}">
                <div class="admin-alert">Không thể xóa tài khoản. Vui lòng thử lại.</div>
            </c:if>

            <section class="admin-panel">
                <div class="admin-panel-header">
                    <div class="admin-panel-title">
                        <h2>Quản lý Tài khoản (User)</h2>
                        <p>Thêm, chỉnh sửa, tìm kiếm, phân trang và xóa tài khoản người dùng.</p>
                    </div>
                    <div class="admin-toolbar">
                        <form class="admin-search" action="${pageContext.request.contextPath}/admin/user/list" method="get">
                            <input type="search" name="keyword" value="<c:out value='${keyword}' />" placeholder="Tìm username, họ tên, email, SĐT" aria-label="Tìm tài khoản">
                            <select name="roleid" aria-label="Lọc theo vai trò">
                                <option value="" ${selectedRole == null ? 'selected' : ''}>Tất cả vai trò</option>
                                <option value="1" ${selectedRole == 1 ? 'selected' : ''}>Admin</option>
                                <option value="2" ${selectedRole == 2 ? 'selected' : ''}>Manager</option>
                                <option value="5" ${selectedRole == 5 ? 'selected' : ''}>Member</option>
                            </select>
                            <button type="submit">Tìm kiếm</button>
                            <c:if test="${not empty keyword || selectedRole != null}">
                                <a class="admin-button admin-button-secondary" href="${pageContext.request.contextPath}/admin/user/list">Đặt lại</a>
                            </c:if>
                        </form>
                        <a class="admin-button admin-button-primary" href="${pageContext.request.contextPath}/admin/user/add?returnTo=users">Thêm tài khoản</a>
                    </div>
                </div>

                <div class="admin-table-wrap">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>ID</th>
                                <th>Tên đăng nhập</th>
                                <th>Họ và tên</th>
                                <th>Email</th>
                                <th>Số điện thoại</th>
                                <th>Vai trò</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${userList}" var="u" varStatus="status">
                                <tr>
                                    <td class="admin-id">${pageOffset + status.index + 1}</td>
                                    <td class="admin-id">#<c:out value="${u.id}" /></td>
                                    <td><strong><c:out value="${u.userName}" /></strong></td>
                                    <td><c:out value="${u.fullName}" /></td>
                                    <td class="admin-id"><c:out value="${not empty u.email ? u.email : '—'}" /></td>
                                    <td class="admin-id"><c:out value="${not empty u.phone ? u.phone : '—'}" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.roleid == 1}"><span class="admin-role">Admin</span></c:when>
                                            <c:when test="${u.roleid == 2}"><span class="admin-role">Manager</span></c:when>
                                            <c:otherwise><span class="admin-role member">Member</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.status == 0}">
                                                <span class="admin-role" style="background:#fff2e8;color:#d4380d;border:1px solid #ffbb96;">Chờ kích hoạt</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="admin-role" style="background:#f6ffed;color:#389e0d;border:1px solid #b7eb8f;">Đã kích hoạt</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="admin-actions">
                                            <c:url value="/admin/user/edit" var="editUrl">
                                                <c:param name="id" value="${u.id}" />
                                                <c:param name="returnTo" value="users" />
                                            </c:url>
                                            <a class="admin-action admin-action-edit" href="${editUrl}">Sửa</a>
                                            <c:if test="${u.id != sessionScope.account.id}">
                                                <c:url value="/admin/user/delete" var="deleteUrl">
                                                    <c:param name="id" value="${u.id}" />
                                                    <c:param name="returnTo" value="users" />
                                                </c:url>
                                                <a class="admin-action admin-action-delete" href="${deleteUrl}">Xóa</a>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty userList}">
                                <tr><td class="admin-empty" colspan="9">Không tìm thấy tài khoản phù hợp.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <c:if test="${filteredUsers > 0}">
                    <nav class="admin-pagination" aria-label="Phân trang tài khoản">
                        <span class="admin-pagination-info">Trang ${currentPage} / ${totalPages} (${filteredUsers} tài khoản)</span>
                        <c:if test="${totalPages > 1}">
                            <div class="admin-pagination-links">
                                <c:if test="${currentPage > 1}">
                                    <c:url value="/admin/user/list" var="prevUrl">
                                        <c:param name="page" value="${currentPage - 1}" />
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${selectedRole != null}"><c:param name="roleid" value="${selectedRole}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link" href="${prevUrl}">Trước</a>
                                </c:if>
                                <c:forEach begin="1" end="${totalPages}" var="p">
                                    <c:url value="/admin/user/list" var="pageUrl">
                                        <c:param name="page" value="${p}" />
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${selectedRole != null}"><c:param name="roleid" value="${selectedRole}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link ${p == currentPage ? 'active' : ''}" href="${pageUrl}">${p}</a>
                                </c:forEach>
                                <c:if test="${currentPage < totalPages}">
                                    <c:url value="/admin/user/list" var="nextUrl">
                                        <c:param name="page" value="${currentPage + 1}" />
                                        <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}" /></c:if>
                                        <c:if test="${selectedRole != null}"><c:param name="roleid" value="${selectedRole}" /></c:if>
                                    </c:url>
                                    <a class="admin-page-link" href="${nextUrl}">Sau</a>
                                </c:if>
                            </div>
                        </c:if>
                    </nav>
                </c:if>
            </section>
        </main>
    </div>
</div>
</body>
</html>
