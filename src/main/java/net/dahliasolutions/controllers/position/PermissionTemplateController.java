package net.dahliasolutions.controllers.position;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.position.PermissionTemplate;
import net.dahliasolutions.models.position.PermissionTemplateModel;
import net.dahliasolutions.models.position.Position;
import net.dahliasolutions.models.position.PositionSelectedModel;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.models.user.UserRoleSelectedModel;
import net.dahliasolutions.models.user.UserRoles;
import net.dahliasolutions.services.RedirectService;
import net.dahliasolutions.services.position.PermissionTemplateService;
import net.dahliasolutions.services.position.PositionService;
import net.dahliasolutions.services.user.UserRolesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/permissiontemplate")
@RequiredArgsConstructor
public class PermissionTemplateController {

    private final PermissionTemplateService permissionTemplateService;
    private final UserRolesService rolesService;
    private final PositionService positionService;
    private final RedirectService redirectService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("moduleTitle", "Settings");
        model.addAttribute("moduleLink", "/admin");
    }

    @GetMapping("")
    public String getPositions(Model model, HttpSession session) {
        redirectService.setHistory(session, "/permissiontemplate");
        List<PermissionTemplate> templateList = permissionTemplateService.findAll();
        model.addAttribute("templateList", templateList);
        return "admin/position/listTemplates";
    }

    @GetMapping("/{id}")
    public String getPosition(@PathVariable BigInteger id, Model model, HttpSession session) {
        Optional<PermissionTemplate> template = permissionTemplateService.findById(id);

        if (template.isEmpty()) {
            session.setAttribute("msgError", "Template Not Found!");
            return redirectService.pathName(session,"permissiontemplate");
        }

        redirectService.setHistory(session, "/permissiontemplate/"+id);
        model.addAttribute("template", template.get());
        return "admin/position/template";
    }

    @GetMapping("/new")
    public String setPosition(Model model) {
        PermissionTemplate template = new PermissionTemplate();
        List<Position> positionList = positionService.findAll();
        List<UserRoles> roleList = rolesService.findAll();

        model.addAttribute("template", template);
        model.addAttribute("positionList", positionList);
        model.addAttribute("roleList", roleList);
        return "admin/position/templateNew";
    }

    @PostMapping("/create")
    public String createPosition(@ModelAttribute PermissionTemplateModel templateModel, Model model, HttpSession session) {
        Optional<PermissionTemplate> existingTemplate = permissionTemplateService.findByName(templateModel.name());
        if(existingTemplate.isPresent()) {
            session.setAttribute("msgError", "Template Name Already Exists!");
            model.addAttribute("template", templateModel);
            return "admin/position/templateNew";
        }

        PermissionTemplate newTemplate = new PermissionTemplate();
            newTemplate.setName(templateModel.name());
            newTemplate.setPosition(positionService.findById(templateModel.positionId()).get());

        List<String> items = Arrays.asList(templateModel.roles().split("\s"));
        List<UserRoles> rolesList = new ArrayList<>();
        for (String s : items) {
            if (!s.equals("")) {
                try {
                    int i = Integer.parseInt(s);
                    Optional<UserRoles> r = rolesService.findById(BigInteger.valueOf(i));
                    if (r.isPresent()) {
                        rolesList.add(r.get());
                    }
                } catch (Error e) {
                    System.out.println(e);
                }
            }
        }
        newTemplate.setUserRoles(rolesList);

        permissionTemplateService.save(newTemplate);

        session.setAttribute("msgSuccess", "Template Successfully Added.");
        return "redirect:/position";
    }

    @GetMapping("/edit/{id}")
    public String editPosition(@PathVariable BigInteger id, Model model, HttpSession session) {
        Optional<PermissionTemplate> template = permissionTemplateService.findById(id);
        if (template.isEmpty()) {
            session.setAttribute("msgError", "Template Not Found!");
            return redirectService.pathName(session,"permissiontemplate");
        }

        List<Position> positionList = positionService.findAll();

        List<UserRoles> allRoleList = rolesService.findAll();
        List<Object> roleList = new ArrayList<>();
        for (UserRoles r : allRoleList) {
            UserRoleSelectedModel selected = new UserRoleSelectedModel();
            selected.setId(r.getId());
            selected.setName(r.getName());
            selected.setDescription(r.getDescription());
            selected.setSelected(false);
            for (UserRoles ur : template.get().getUserRoles()) {
                if (ur.getId().equals(r.getId())) {
                    selected.setSelected(true);
                }
            }
            roleList.add(selected);
        }

        model.addAttribute("template", template.get());
        model.addAttribute("positionList", positionList);
        model.addAttribute("roleList", roleList);
        redirectService.setHistory(session, "/permissiontemplate/edit/"+id);
        return "admin/position/templateEdit";
    }

    @PostMapping("/update")
    public String updatePosition(@ModelAttribute PermissionTemplateModel templateModel, Model model, HttpSession session) {
        Optional<PermissionTemplate> template = permissionTemplateService.findById(templateModel.id());
        Optional<PermissionTemplate> existingTemplate = permissionTemplateService.findByName(templateModel.name());
        if(template.isEmpty()) {
            session.setAttribute("msgError", "Template Not Found!");
            model.addAttribute("template", templateModel);
            return "admin/position/templateNew";
        }
        if(existingTemplate.isPresent()) {
            if (!existingTemplate.get().getId().equals(template.get().getId())) {
                session.setAttribute("msgError", "Template Name Already Exists!");
                model.addAttribute("template", templateModel);
                return redirectService.pathName(session, "position");
            }
        }

        template.get().setName(templateModel.name());
        template.get().setPosition(positionService.findById(templateModel.positionId()).get());

        List<String> items = Arrays.asList(templateModel.roles().split("\s"));
        List<UserRoles> rolesList = new ArrayList<>();
        for (String s : items) {
            if (!s.equals("")) {
                try {
                    int i = Integer.parseInt(s);
                    Optional<UserRoles> r = rolesService.findById(BigInteger.valueOf(i));
                    if (r.isPresent()) {
                        rolesList.add(r.get());
                    }
                } catch (Error e) {
                    System.out.println(e);
                }
            }
        }
        template.get().setUserRoles(rolesList);

        permissionTemplateService.save(template.get());

        session.setAttribute("msgSuccess", "Template Successfully Updated.");
        return "redirect:/position";
    }

    @GetMapping("/{id}/delete")
    public String deletePosition(@PathVariable BigInteger id, HttpSession session) {
        permissionTemplateService.deletePermissionTemplateById(id);
        return "redirect:/position";
    }
}
