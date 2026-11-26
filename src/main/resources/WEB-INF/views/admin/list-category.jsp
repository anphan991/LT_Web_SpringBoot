<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản trị - ShopSpringBoot</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261119-v3">
</head>
<body class="admin-page">
<div class="admin-shell">
    <jsp:include page="/WEB-INF/commons/admin/left.jsp" />

    <div class="admin-workspace">

        <main class="admin-main">
            <div class="admin-stats">
                <div class="admin-stat">
                    <div class="admin-stat-label">Danh mục</div>
                    <div class="admin-stat-value">${totalCategories}</div>
                </div>
                <div class="admin-stat">
                    <div class="admin-stat-label">Tài khoản</div>
                    <div class="admin-stat-value">${not empty userList ? userList.size() : 0}</div>
                </div>
                <div class="admin-stat">
                    <div class="admin-stat-label">Quyền hiện tại</div>
                    <div class="admin-stat-value">CRUD</div>
                </div>
            </div>

            <c:if test="${not empty dbError}">
                <div class="admin-alert"><c:out value="${dbError}" /></div>
            </c:if>
            <c:if test="${not empty userError}">
                <div class="admin-alert"><c:out value="${userError}" /></div>
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
            <c:if test="${param.categoryDeleted == '1'}">
                <div class="admin-alert admin-alert-success">Đã xóa danh mục.</div>
            </c:if>
            <c:if test="${param.categoryError == 'missing'}">
                <div class="admin-alert">Không tìm thấy danh mục cần thao tác.</div>
            </c:if>
            <c:if test="${param.categoryError == 'delete'}">
                <div class="admin-alert">Không thể xóa danh mục. Vui lòng thử lại.</div>
            </c:if>
            <c:if test="${param.categoryError == 'load'}">
                <div class="admin-alert">Không thể tải thông tin danh mục. Vui lòng thử lại.</div>
            </c:if>

            <section class="admin-panel">
                <div class="admin-panel-header">
                    <div class="admin-panel-title">
                        <h2>Danh mục sản phẩm</h2>
                        <p>Thêm, chỉnh sửa hoặc xóa danh mục.</p>
                    </div>
                    <div class="admin-toolbar">
                        <form class="admin-search" action="${pageContext.request.contextPath}/admin/category/list" method="get">
                            <input type="search" name="keyword" value="<c:out value='${keyword}' />" placeholder="Tìm danh mục" aria-label="Tìm danh mục">
                            <button type="submit">Tìm kiếm</button>
                        </form>
                        <a class="admin-button admin-button-primary" href="<c:url value='/admin/category/add'/>">Thêm danh mục</a>
                    </div>
                </div>

                <div class="admin-table-wrap">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>Hình ảnh</th>
                                <th>Tên danh mục</th>
                                <th>ID</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${cateList}" var="cate" varStatus="status">
                                <tr>
                                    <td class="admin-id">${pageOffset + status.index + 1}</td>
                                    <c:url value="/image" var="imgUrl">
                                        <c:param name="fname" value="${cate.icon}" />
                                    </c:url>
                                    <td><div class="admin-thumb"><img src="${cate.externalIcon ? cate.icon : imgUrl}" alt="<c:out value='${cate.catename}' />"></div></td>
                                    <td><strong><c:out value="${cate.catename}" /></strong></td>
                                    <td class="admin-id">#<c:out value="${cate.id}" /></td>
                                    <td>
                                        <div class="admin-actions">
                                            <c:url value="/admin/category/edit" var="editUrl">
                                                <c:param name="id" value="${cate.id}" />
                                            </c:url>
                                            <c:url value="/admin/category/delete" var="deleteUrl">
                                                <c:param name="id" value="${cate.id}" />
                                            </c:url>
                                            <a class="admin-action admin-action-edit" href="${editUrl}">Sửa</a>
                                            <a class="admin-action admin-action-delete" href="${deleteUrl}">Xóa</a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty cateList}">
                                <tr><td class="admin-empty" colspan="5">Không tìm thấy danh mục phù hợp.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <c:if test="${totalPages > 1}">
                    <nav class="admin-pagination" aria-label="Phân trang danh mục">
                        <span class="admin-pagination-info">Trang ${currentPage} / ${totalPages} (${filteredCategories} danh mục)</span>
                        <div class="admin-pagination-links">
                            <c:if test="${currentPage > 1}">
                                <c:url value="/admin/category/list" var="prevUrl">
                                    <c:param name="page" value="${currentPage - 1}" />
                                    <c:if test="${not empty keyword}">
                                        <c:param name="keyword" value="${keyword}" />
                                    </c:if>
                                </c:url>
                                <a class="admin-page-link" href="${prevUrl}">Trước</a>
                            </c:if>
                            <c:forEach begin="1" end="${totalPages}" var="p">
                                <c:url value="/admin/category/list" var="pageUrl">
                                    <c:param name="page" value="${p}" />
                                    <c:if test="${not empty keyword}">
                                        <c:param name="keyword" value="${keyword}" />
                                    </c:if>
                                </c:url>
                                <a class="admin-page-link ${p == currentPage ? 'active' : ''}" href="${pageUrl}">${p}</a>
                            </c:forEach>
                            <c:if test="${currentPage < totalPages}">
                                <c:url value="/admin/category/list" var="nextUrl">
                                    <c:param name="page" value="${currentPage + 1}" />
                                    <c:if test="${not empty keyword}">
                                        <c:param name="keyword" value="${keyword}" />
                                    </c:if>
                                </c:url>
                                <a class="admin-page-link" href="${nextUrl}">Sau</a>
                            </c:if>
                        </div>
                    </nav>
                </c:if>
            </section>

            <section class="admin-panel" id="users-section">
                <div class="admin-panel-header">
                    <div class="admin-panel-title">
                        <h2>Tài khoản</h2>
                        <p>Quản lý tài khoản và quyền truy cập.</p>
                    </div>
                    <c:if test="${sessionScope.account.roleid == 1}">
                        <a class="admin-button admin-button-primary" href="${pageContext.request.contextPath}/admin/user/add">Thêm tài khoản</a>
                    </c:if>
                </div>

                <div class="admin-table-wrap">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Tên đăng nhập</th>
                                <th>Họ và tên</th>
                                <th>Liên hệ</th>
                                <th>Vai trò</th>
                                <c:if test="${sessionScope.account.roleid == 1}"><th>Thao tác</th></c:if>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${userList}" var="u">
                                <tr>
                                    <td class="admin-id">#<c:out value="${u.id}" /></td>
                                    <td><strong><c:out value="${u.userName}" /></strong></td>
                                    <td><c:out value="${u.fullName}" /></td>
                                    <td class="admin-id"><c:out value="${not empty u.email ? u.email : '—'}" /><c:if test="${not empty u.phone}"> - <c:out value="${u.phone}" /></c:if></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.roleid == 1}"><span class="admin-role">Admin</span></c:when>
                                            <c:when test="${u.roleid == 2}"><span class="admin-role">Manager</span></c:when>
                                            <c:otherwise><span class="admin-role member">Member</span></c:otherwise>
                                        </c:choose>
                                        <c:choose>
                                            <c:when test="${u.status == 0}">
                                                <span class="admin-role" style="background:#fff2e8;color:#d4380d;border:1px solid #ffbb96;margin-left:4px;">Chờ kích hoạt</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="admin-role" style="background:#f6ffed;color:#389e0d;border:1px solid #b7eb8f;margin-left:4px;">Đã kích hoạt</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <c:if test="${sessionScope.account.roleid == 1}">
                                        <td>
                                            <div class="admin-actions">
                                                <c:url value="/admin/user/edit" var="userEditUrl">
                                                    <c:param name="id" value="${u.id}" />
                                                </c:url>
                                                <a class="admin-action admin-action-edit" href="${userEditUrl}">Sửa</a>
                                                <c:if test="${u.id != sessionScope.account.id}">
                                                    <form class="admin-inline-form" action="${pageContext.request.contextPath}/admin/user/delete" method="post" onsubmit="return confirm('Bạn có chắc muốn xóa tài khoản này không?');">
                                                        <input type="hidden" name="id" value="${u.id}">
                                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}' />">
                                                        <button class="admin-action admin-action-delete" type="submit">Xóa</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty userList}">
                                <tr><td class="admin-empty" colspan="6">Chưa có tài khoản để hiển thị.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>
</div>
</body>
</html>
