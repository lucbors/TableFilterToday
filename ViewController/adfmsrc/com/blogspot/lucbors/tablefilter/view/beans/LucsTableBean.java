package com.blogspot.lucbors.tablefilter.view.beans;

import com.blogspot.lucbors.tablefilter.view.utils.JSFUtils;

import java.io.Serializable;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.event.QueryEvent;
import oracle.adf.view.rich.model.AttributeCriterion;
import oracle.adf.view.rich.model.AttributeDescriptor;
import oracle.adf.view.rich.model.ConjunctionCriterion;
import oracle.adf.view.rich.model.Criterion;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;

import oracle.adfinternal.view.faces.model.binding.FacesCtrlSearchBinding;

public class LucsTableBean implements Serializable {
    private transient RichTable table;
    private boolean initialQuery = true;

    // The map with filterCriteria. Injected as managed property.
    private Map<String, Object> defaultFilterCriteria;
    // The name of the querybinding. Injected as managed property.
    private String queryBindingName;

    public LucsTableBean() {
        super();
    }


    public FilterableQueryDescriptor getCustomQueryDescriptorHardcoded() {
        String bindingEl = "#{bindings.AllEmployeesQuery}";

        FacesCtrlSearchBinding sbinding = (FacesCtrlSearchBinding) JSFUtils.resolveExpression(bindingEl);
        FilterableQueryDescriptor fqd = (FilterableQueryDescriptor) sbinding.getQueryDescriptor();

        if (fqd != null && fqd.getFilterConjunctionCriterion() != null && isInitialQuery()) {
            ConjunctionCriterion cc = fqd.getFilterConjunctionCriterion();
            List<Criterion> lc = cc.getCriterionList();
            for (Criterion c : lc) {
                if (c instanceof AttributeCriterion) {
                    AttributeCriterion ac = (AttributeCriterion) c;
                    if ((ac.getAttribute().getName().equalsIgnoreCase("HireDate")) && (ac.getValue() == null)) {
                        // we need date without time so lets call getToday()
                        ac.setValue(getToday());
                    }
                }
            }
            setInitialQuery(false);
            RichTable tbl = getTable();
            QueryEvent queryEvent = new QueryEvent(tbl, fqd);
            sbinding.processQuery(queryEvent);
        }
        return fqd;
    }

    public FilterableQueryDescriptor getCustomQueryDescriptor() {
        String bindingEl = "#{bindings." + queryBindingName + "}";

        FacesCtrlSearchBinding sbinding = (FacesCtrlSearchBinding) JSFUtils.resolveExpression(bindingEl);
        FilterableQueryDescriptor fqd = (FilterableQueryDescriptor) sbinding.getQueryDescriptor();

        // Only if there are any defaults defined
        if (defaultFilterCriteria != null && !defaultFilterCriteria.isEmpty()) {

            if (fqd != null && fqd.getFilterConjunctionCriterion() != null && isInitialQuery()) {
                ConjunctionCriterion cc = fqd.getFilterConjunctionCriterion();
                List<Criterion> lc = cc.getCriterionList();
                for (Criterion c : lc) {
                    if (c instanceof AttributeCriterion) {
                        AttributeCriterion ac = (AttributeCriterion) c;
                        // check if a default filter exists for this attribute
                        Object filter = defaultFilterCriteria.get(ac.getAttribute().getName());
                        if (filter != null) {
                            // OK, this might not be optimal, but I try here to get the default component type
                            // if it is an input date, we need to parse the String value to a Date
                            if (ac.getComponentType(null) == AttributeDescriptor.ComponentType.inputDate) {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                try {
                                    if (filter.toString().equalsIgnoreCase("TODAY")) {
                                        ac.setValue(getToday());
                                    } else {
                                        Date date = formatter.parse(filter.toString());
                                        ac.setValue(date);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                ac.setValue(filter);
                            }
                        }
                    }
                }
            }
            setInitialQuery(false);
            RichTable tbl = getTable();
            QueryEvent queryEvent = new QueryEvent(tbl, fqd);
            sbinding.processQuery(queryEvent);
        }

        return fqd;
    }

    public Date getToday() {
        Date today = new Date();
        // Get Calendar object set to the date and time of the given Date object
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        // Set time fields to zero
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // Put it back in the Date object
        today = cal.getTime();
        return today;
    }

    public void setQueryBindingName(String queryBindingName) {
        this.queryBindingName = queryBindingName;
    }

    public void setDefaultFilterCriteria(Map<String, Object> defaultFilterCriteria) {
        this.defaultFilterCriteria = defaultFilterCriteria;
    }

    public Map<String, Object> getDefaultFilterCriteria() {
        return defaultFilterCriteria;
    }

    public void setTable(RichTable table) {
        this.table = table;
    }

    public RichTable getTable() {
        return table;
    }

    public void setInitialQuery(boolean initialQuery) {
        this.initialQuery = initialQuery;
    }

    public boolean isInitialQuery() {
        return initialQuery;
    }

}
