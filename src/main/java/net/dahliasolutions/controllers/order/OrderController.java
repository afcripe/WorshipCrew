package net.dahliasolutions.controllers.order;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.EventModule;
import net.dahliasolutions.models.EventType;
import net.dahliasolutions.models.Notification;
import net.dahliasolutions.models.UniversalSearchModel;
import net.dahliasolutions.models.campus.Campus;
import net.dahliasolutions.models.department.DepartmentRegional;
import net.dahliasolutions.models.order.*;
import net.dahliasolutions.models.records.BigIntegerStringModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.NotificationService;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.campus.CampusService;
import net.dahliasolutions.services.department.DepartmentRegionalService;
import net.dahliasolutions.services.order.OrderService;
import net.dahliasolutions.services.order.OrderNoteService;
import net.dahliasolutions.services.order.OrderItemService;
import net.dahliasolutions.services.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Math.abs;

@Controller
@RequestMapping("/request")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderNoteService orderNoteService;
    private final UserService userService;
    private final DepartmentRegionalService departmentService;
    private final CampusService campusService;
    private final RedirectService redirectService;
    private final NotificationService notificationService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Requests");
        model.addAttribute("moduleLink", "/request");
    }

    @GetMapping("")
    public String getSupervisorOrders(Model model, HttpSession session) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<OrderItem> openItemList = orderItemService.findAllBySupervisorOpenOnly(user);
        List<OrderRequest> openOrderList = orderService.findAllBySupervisorOpenOnly(user);
        List<OrderRequest> orderMentionList = orderService.findAllByMentionOpenOnly(user);

        model.addAttribute("editable", false);
        model.addAttribute("user", user);
        model.addAttribute("openItemList", openItemList);
        model.addAttribute("openOrderList", openOrderList);
        model.addAttribute("orderMentionList", orderMentionList);
        redirectService.setHistory(session, "/request");
        return "order/orderList";
    }

    @GetMapping("/settings")
    public String getStoreSettings(Model model) {
        List<Notification> notificationList = notificationService.findAllByModule(EventModule.Request);
        List<User> users = userService.findAll();
        List<BigIntegerStringModel> notificationUsers = new ArrayList<>();
        for (User u : users) {
            notificationUsers.add(new BigIntegerStringModel(u.getId(), u.getFirstName()+' '+u.getLastName()));
        }

        List<OrderRequest> requests = orderService.findAll();
        Integer completeRequests = 0;
        Integer openRequests = 0;
        for (OrderRequest request : requests) {
            if (request.getOrderStatus().equals(OrderStatus.Complete)) {
                completeRequests++;
            }
            if (!request.getOrderStatus().equals(OrderStatus.Complete) || !request.getOrderStatus().equals(OrderStatus.Cancelled)) {
                openRequests++;
            }
        }

        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("openRequests", openRequests);
        model.addAttribute("completeRequests", completeRequests);

        model.addAttribute("notificationList", notificationList);
        model.addAttribute("typeList", Arrays.asList(EventType.values()));
        model.addAttribute("notificationUsers", notificationUsers);
        return "order/settings";
    }

    @GetMapping("/user/{id}")
    public String getUserOrders(@RequestParam Optional<String> cycle, @PathVariable BigInteger id, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        Optional<User> user = userService.findById(id);

        if (user.isEmpty()) {
            session.setAttribute("msgError", "User not found.");
            return redirectService.pathName(session, "/request");
        }

        List<OrderRequest> orderList = orderService.findAllByUserAndCycle(user.get().getId(), startDate, endDate);
        List<OrderItem> itemList = orderItemService.findAllBySupervisorAndCycle(user.get().getId(), startDate, endDate);
        List<OrderRequest> supervisorOrderList = orderService.findAllBySupervisorAndCycle(user.get().getId(), startDate, endDate);
        List<OrderRequest> orderMentionList = orderService.findAllByMentionOpenAndCycle(user.get().getId(), startDate, endDate);

        model.addAttribute("editable", false);
        model.addAttribute("searchedUser", user.get().getFirstName()+" "+user.get().getLastName());
        model.addAttribute("orderList", orderList);
        model.addAttribute("itemList", itemList);
        model.addAttribute("supervisorOrderList", supervisorOrderList);
        model.addAttribute("orderMentionList", orderMentionList);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        redirectService.setHistory(session, "/request/user/"+id);
        return "order/orderUser";
    }

    @GetMapping("/department")
    public String getOpenByDepartment(@RequestParam Optional<String> cycle, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        List<DepartmentRegional> departmentList = departmentService.findAll();

        List<OrderItemDepartment> departmentItemList = new ArrayList<>();
        for (DepartmentRegional department : departmentList) {
            OrderItemDepartment departmentItem = new OrderItemDepartment(department, new ArrayList<>());
            departmentItem.setOrderItemList(orderItemService.findAllByDepartmentAndCycle(department.getId(), startDate, endDate));
            departmentItemList.add(departmentItem);
        }


        model.addAttribute("editable", false);
        model.addAttribute("departmentItemList", departmentItemList);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        redirectService.setHistory(session, "/request/department");
        return "order/orderDepartmentList";
    }

    @GetMapping("/department/{name}")
    public String getDepartmentOrders(@RequestParam Optional<String> cycle, @PathVariable String name, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        Optional<DepartmentRegional> department = departmentService.findByName(name);

        if (department.isEmpty()) {
            session.setAttribute("msgError", "Department not found.");
            return redirectService.pathName(session, "/request");
        }

        List<OrderItem> itemList = orderItemService.findAllByDepartmentAndCycle(department.get().getId(), startDate, endDate);
        List<OrderItem> openItemList = new ArrayList<>();
        List<OrderItem> closedItemList = new ArrayList<>();

        for (OrderItem item : itemList) {
            if (item.getItemStatus().equals(OrderStatus.Complete) || item.getItemStatus().equals(OrderStatus.Cancelled)) {
                closedItemList.add(item);
            }else {
                openItemList.add(item);

            }
        }

        model.addAttribute("editable", false);
        model.addAttribute("department", department.get());
        model.addAttribute("openItemList", openItemList);
        model.addAttribute("closedItemList", closedItemList);
        redirectService.setHistory(session, "/request/department/"+name);
        return "order/orderDepartment";
    }

    @GetMapping("/campus")
    public String getOpenByCampus(@RequestParam Optional<String> cycle, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        List<Campus> campusList = campusService.findAll();

        List<OrderRequestCampus> campusItemList = new ArrayList<>();
        for (Campus campus : campusList) {
            OrderRequestCampus campusItem = new OrderRequestCampus(campus, new ArrayList<>());
                            campusItem.setRequestList(orderService.findAllByCampusAndCycle(campus.getId(), startDate, endDate));
            campusItemList.add(campusItem);
        }


        model.addAttribute("editable", false);
        model.addAttribute("campusItemList", campusItemList);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        redirectService.setHistory(session, "/request/campus");
        return "order/orderCampusList";
    }

    @GetMapping("/campus/{name}")
    public String getCampusOrders(@RequestParam Optional<String> cycle, @PathVariable String name, Model model, HttpSession session) {
        Integer currentCycle = Integer.parseInt(session.getAttribute("cycle").toString());
        if (cycle.isPresent()) {
            switch (cycle.get()) {
                case "left":
                    currentCycle--;
                    session.setAttribute("cycle", currentCycle);
                    break;
                case "right":
                    if (currentCycle < 0) {
                        currentCycle++;
                        session.setAttribute("cycle", currentCycle);
                        break;
                    }
                default:
                    currentCycle=0;
                    session.setAttribute("cycle", 0);
            }
        }

        LocalDateTime startDate = getStartDate(session.getAttribute("dateFilter").toString(), currentCycle);
        LocalDateTime endDate = getEndDate(session.getAttribute("dateFilter").toString(), currentCycle);

        Optional<Campus> campus = campusService.findByName(name);

        if (campus.isEmpty()) {
            session.setAttribute("msgError", "Campus not found.");
            return redirectService.pathName(session, "/request");
        }

        List<OrderRequest> itemList = orderService.findAllByCampusAndCycle(campus.get().getId(), startDate, endDate);
        List<OrderRequest> openOrderList = new ArrayList<>();
        List<OrderRequest> closedOrderList = new ArrayList<>();

        for (OrderRequest item : itemList) {
            if (item.getOrderStatus().equals(OrderStatus.Complete) || item.getOrderStatus().equals(OrderStatus.Cancelled)) {
                closedOrderList.add(item);
            }else {
                openOrderList.add(item);

            }
        }

        model.addAttribute("editable", false);
        model.addAttribute("campus", campus.get());
        model.addAttribute("openOrderList", openOrderList);
        model.addAttribute("closedOrderList", closedOrderList);
        redirectService.setHistory(session, "/request/campus/"+name);
        return "order/orderCampus";
    }

    @GetMapping("/order/{id}")
    public String getOrder(@PathVariable BigInteger id, Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        Optional<OrderRequest> request = orderService.findById(id);
        if (request.isEmpty()) {
            session.setAttribute("msgError", "Request not found.");
            return redirectService.pathName(session, "/request");
        }
        // get Edit permission
        boolean editable = false;
        if (allowEdit(request.get())) {
            editable = true;
        }
        List<OrderNote> noteList = orderNoteService.findByOrderId(id);

        model.addAttribute("editable", editable);
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("currentSuperId", request.get().getSupervisor().getId());
        model.addAttribute("orderRequest", request.get());
        model.addAttribute("noteList", noteList);
        redirectService.setHistory(session, "/request/order"+request.get().getId());
        return "order/order";
    }

    @PostMapping("/search")
    public String searchRequests(@ModelAttribute UniversalSearchModel searchModel) {
        // determine if search type
        switch (searchModel.getSearchType()) {
            case "request":
                return "redirect:/request/order/"+searchModel.getSearchId();
            case "user":
                return "redirect:/request/user/"+searchModel.getSearchId();
            default:
                return "redirect:/request";
        }
    }

    /*  Determine Edit Permissions */
    private boolean allowEdit(OrderRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Collection<UserRoles> roles = user.getUserRoles();

        for (UserRoles role : roles){
            if (role.getName().equals("ADMIN_WRITE") || role.getName().equals("REQUEST_SUPERVISOR")) {
                return true;
            }
        }
        if (request.getSupervisor().equals(user)) {
            return true;
        }
        for (OrderItem item : request.getRequestItems()) {
            if (item.getSupervisor().equals(user)) {
                return true;
            }
        }
        return false;
    }

    private LocalDateTime getStartDate(String dateSpan, Integer cycle) {
        // parse dateSpan
        String span = dateSpan.substring(0,1);
        String part = dateSpan.substring(1,2);
        //adjust the cycle
        int adjustment = Integer.parseInt(span);
        int cycleAdjustment = adjustment+abs(cycle*adjustment);

        // init date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateNow = LocalDateTime.now();
        // add leading 0 to month
        Integer month = dateNow.getMonthValue();
        String monthString = month.toString();
        Integer day = dateNow.getDayOfMonth();
        String dayString = day.toString();
        if (monthString.length() < 2) {
            monthString = "0"+monthString;
        }
        if (dayString.length() < 2) {
            dayString = "0"+dayString;
        }
        // create date and convert to LocalDateTime
        String dateString = dateNow.getYear()+"-"+monthString+"-"+ dayString+" 00:00";
        LocalDateTime returnDate = LocalDateTime.parse(dateString, formatter);

        // adjust date based on part
        switch (part) {
            case "Y":
                returnDate = returnDate.minusYears(cycleAdjustment);
                break;
            case "M":
                returnDate = returnDate.minusMonths(cycleAdjustment);
                break;
            case "W":
                returnDate = returnDate.minusWeeks(cycleAdjustment);
                break;
        }
        return returnDate;
    }

    private LocalDateTime getEndDate(String dateSpan, Integer cycle) {
        // parse dateSpan
        String part = dateSpan.substring(1,2);
        //adjust the cycle
        int adjustment = 0;
        int cycleAdjustment = adjustment+abs(cycle);

        // init date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateNow = LocalDateTime.now();
        // add leading 0 to month
        Integer month = dateNow.getMonthValue();
        String monthString = month.toString();
        Integer day = dateNow.getDayOfMonth();
        String dayString = day.toString();
        if (monthString.length() < 2) {
            monthString = "0"+monthString;
        }
        if (dayString.length() < 2) {
            dayString = "0"+dayString;
        }
        // create date and convert to LocalDateTime
        String dateString = dateNow.getYear()+"-"+monthString+"-"+ dayString+" 24:00";
        LocalDateTime returnDate = LocalDateTime.parse(dateString, formatter);

        // adjust date based on part
        switch (part) {
            case "Y":
                returnDate = returnDate.minusYears(cycleAdjustment);
                break;
            case "M":
                returnDate = returnDate.minusMonths(cycleAdjustment);
                break;
            case "W":
                returnDate = returnDate.minusWeeks(cycleAdjustment);
                break;
        }
        return returnDate;
    }


}
