package net.dahliasolutions.services.wiki;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.data.WikiNavigatorRepository;
import net.dahliasolutions.models.wiki.WikiNavigator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WikiNavigatorService implements WikiNavigatorServiceInterface{

    private final WikiNavigatorRepository navigatorRepository;

    @Override
    public WikiNavigator createItem(WikiNavigator navigator) {
        Integer providedOrderInt = navigator.getItemOrder();
        Integer newOrderInt = findAll().size();
        navigator.setItemOrder(newOrderInt+1);

        if (newOrderInt.equals(0) || providedOrderInt.equals(0)) {
            // if no order provided, set next in list
            return navigatorRepository.save(navigator);
        } else {
            // if order provided, set next in list, save, re-order the list
            WikiNavigator navItem  = navigatorRepository.save(navigator);
            navItem.setItemOrder(providedOrderInt);
            updateItemOrder(navItem, providedOrderInt);
            return navItem;
        }
    }

    @Override
    public List<WikiNavigator> findAll() {
        return navigatorRepository.findAllOrderByItemOrder();
    }

    @Override
    public Optional<WikiNavigator> findById(Integer id) {
        return navigatorRepository.findById(id);
    }

    @Override
    public Optional<WikiNavigator> findByName(String name) {
        return navigatorRepository.findByName(name);
    }

    @Override
    public WikiNavigator save(WikiNavigator navigator) {
        Optional<WikiNavigator> navItem  = navigatorRepository.findById(navigator.getId());
        if (navItem.isPresent()) {
            // get original item order
            Integer providedOrderInt = navigator.getItemOrder();

            // make sure order is positive and >= 1
            if (navigator.getItemOrder() < 1) {
                navigator.setItemOrder(1);
            }

            // save updated item
            navigatorRepository.save(navigator);

            // re-order the list
            updateItemOrder(navItem.get(), providedOrderInt);
            return navigator;
        }
        return null;
    }

    @Override
    public void deleteById(Integer id) {
        navigatorRepository.deleteById(id);
        updateItemOrder();
    }


    private void updateItemOrder() {
        List<WikiNavigator> navItems = findAll();
        List<WikiNavigator> orderedItems = new ArrayList<>();
        Integer i = 1;

        for (WikiNavigator nav : navItems) {
            // loop over each item
            nav.setItemOrder(i);
            orderedItems.add(nav);
            i++;
        }

        // save list order positions
        for (WikiNavigator item : orderedItems) {
            navigatorRepository.save(item);
        }
    }

    private void updateItemOrder(WikiNavigator navigator, Integer originalInt) {
        List<WikiNavigator> navItems = findAll();
        List<WikiNavigator> orderedItems = new ArrayList<>();
        Integer i = 1;
        Integer n = navigator.getItemOrder();

        navItems.add(n-1, navigator);

        for (WikiNavigator nav : navItems) {
            // loop over each item
            // if the item is moved item and old position, skip it
            if(nav.getId().equals(navigator.getId())) {
                if(i.equals(n)){
                    nav.setItemOrder(i);
                    orderedItems.add(nav);
                }
            } else {
                nav.setItemOrder(i);
                orderedItems.add(nav);
            }
            i++;
        }

        // save list order positions
        for (WikiNavigator item : orderedItems) {
            navigatorRepository.save(item);
        }
    }
}
