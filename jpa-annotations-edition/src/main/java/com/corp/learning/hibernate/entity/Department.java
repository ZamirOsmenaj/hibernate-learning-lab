package com.corp.learning.hibernate.entity;

import com.corp.learning.hibernate.entity.component.BudgetEntry;
import org.hibernate.annotations.BatchSize;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Department belongs to a Company (@ManyToOne, the OWNING side of that
 * association - it holds the foreign key column company_id).
 *
 * Also demonstrates three different collection-mapping styles side by side,
 * same as the XML edition:
 *   - employees     -> @OneToMany of entities (mappedBy, i.e. inverse)
 *   - budgetHistory -> @ElementCollection + @OrderColumn (ordered value objects)
 *   - tags          -> @ElementCollection @MapKeyColumn (key/value pairs)
 */
@Entity
@Table(name = "departments")
@BatchSize(size = 10)
// @BatchSize is a Hibernate-specific extension (org.hibernate.annotations),
// not part of the JPA spec - plain JPA has no standard "batch fetch size"
// annotation. This is the direct equivalent of hbm's <class batch-size="10">:
// when several Department proxies need initializing in the same session,
// Hibernate fetches them in batches of up to 10 with a single
// "where id in (?,?,?...)" query instead of one query per proxy. See
// Demo05_FetchingStrategies.
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Version
    @Column(name = "version")
    private int version;

    /**
     * MANY-TO-ONE Department -> Company. This is the OWNING side of the
     * relationship (holds the FK column). fetch = LAZY means: loading a
     * Department does NOT join to companies; the Company is fetched lazily
     * the first time getCompany() is touched, with its own SELECT. Contrast
     * this with Employee.department below, which is EAGER + join-fetched.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /**
     * ONE-TO-MANY Department -> Employee, lazy + batch-size fetch strategy
     * (via @BatchSize on the collection itself, matching hbm's
     * <set ... batch-size="10">): see Demo05.
     */
    @OneToMany(mappedBy = "department", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 10)
    private Set<Employee> employees = new HashSet<>();

    /**
     * @ElementCollection + @OrderColumn (ORDERED, allows duplicates)
     * Backed by an extra "position" column in the child table that the
     * provider manages for you (0-based index) - the annotation equivalent
     * of hbm's &lt;list&gt;. Reach for this mapping when insertion ORDER
     * matters and must survive a reload, e.g. a chronological history log
     * like this budget history.
     *
     * The elements themselves are @Embeddable (BudgetEntry) - a value type
     * with no identity of its own, embedded per-row into the collection
     * table "department_budget_history". @ElementCollection is always fully
     * owned by its parent (equivalent of hbm's collection cascade="all" -
     * there's no separate cascade attribute to set; it's implicit).
     */
    @ElementCollection
    @CollectionTable(name = "department_budget_history", joinColumns = @JoinColumn(name = "department_id"))
    @OrderColumn(name = "position")
    @AttributeOverrides({
            @AttributeOverride(name = "year", column = @Column(name = "budget_year")),
            @AttributeOverride(name = "amount", column = @Column(name = "budget_amount")),
            @AttributeOverride(name = "note", column = @Column(name = "budget_note", length = 255))
    })
    private List<BudgetEntry> budgetHistory = new ArrayList<>();

    /**
     * @ElementCollection of key/value pairs - the annotation equivalent of
     * hbm's &lt;map&gt;. Reach for this whenever the natural shape of the
     * data IS a dictionary, e.g. free-form department attributes/tags.
     * Backed by a simple two-column table (department_id, tag_key) +
     * tag_value, with (department_id, tag_key) effectively unique.
     */
    @ElementCollection
    @CollectionTable(name = "department_tags", joinColumns = @JoinColumn(name = "department_id"))
    @MapKeyColumn(name = "tag_key", length = 50)
    @Column(name = "tag_value", length = 255)
    private Map<String, String> tags = new HashMap<>();

    public Department() {
    }

    public Department(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public List<BudgetEntry> getBudgetHistory() {
        return budgetHistory;
    }

    public void setBudgetHistory(List<BudgetEntry> budgetHistory) {
        this.budgetHistory = budgetHistory;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }

    @Override
    public String toString() {
        return "Department{id=" + id + ", name='" + name + "'}";
    }
}
