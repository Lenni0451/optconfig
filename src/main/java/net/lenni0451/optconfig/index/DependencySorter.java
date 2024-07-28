package net.lenni0451.optconfig.index;

import net.lenni0451.optconfig.exceptions.CircularDependencyException;
import net.lenni0451.optconfig.index.types.ConfigOption;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Internal
public class DependencySorter {

    /**
     * Sorts a list of ConfigOption objects based on their dependencies.<br>
     * Throws a CircularDependencyException if a circular dependency is detected.
     *
     * @param options The list of ConfigOption objects to sort
     * @return The sorted list of ConfigOption objects
     * @throws CircularDependencyException If a circular dependency is detected
     */
    public static List<ConfigOption> sortOptions(final List<ConfigOption> options) {
        Map<String, ConfigOption> optionMap = new HashMap<>(); //Map to store ConfigOption objects by their name
        Map<String, List<String>> dependencyGraph = new HashMap<>(); //Dependency graph where the key is an option name and the value is a list of options dependent on it

        //Populate the optionMap and dependencyGraph
        for (ConfigOption option : options) {
            String optionName = option.getName();
            optionMap.put(optionName, option);
            dependencyGraph.putIfAbsent(optionName, new ArrayList<>());

            for (String dependency : option.getDependencies()) {
                dependencyGraph.putIfAbsent(dependency, new ArrayList<>());
                dependencyGraph.get(dependency).add(optionName);
            }
        }

        //Perform topological sort on the dependency graph
        return topologicalSort(optionMap, dependencyGraph);
    }

    /**
     * Performs a topological sort on the dependency graph.
     *
     * @param optionMap       Map of option names to ConfigOption objects
     * @param dependencyGraph The dependency graph
     * @return The sorted list of ConfigOption objects
     * @throws CircularDependencyException If a circular dependency is detected
     */
    private static List<ConfigOption> topologicalSort(final Map<String, ConfigOption> optionMap, final Map<String, List<String>> dependencyGraph) {
        List<ConfigOption> sortedOptions = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        Stack<String> path = new Stack<>();

        //Perform DFS on each node in the graph
        for (String option : dependencyGraph.keySet()) {
            if (!visited.contains(option)) {
                if (!dfs(option, optionMap, dependencyGraph, visited, visiting, sortedOptions, path)) {
                    throw new CircularDependencyException("Circular option dependency detected: " + path);
                }
            }
        }

        //Reverse the sorted options to get the correct order
        Collections.reverse(sortedOptions);
        return sortedOptions;
    }

    /**
     * Depth-first search (DFS) to visit nodes and detect cycles.
     *
     * @param current         The current node being visited
     * @param optionMap       Map of option names to ConfigOption objects
     * @param dependencyGraph The dependency graph
     * @param visited         Set of visited nodes
     * @param visiting        Set of nodes being visited (used to detect cycles)
     * @param sortedOptions   List to store the sorted options
     * @param path            Stack to track the current path for detecting cycles
     * @return True if no cycle is detected, otherwise false
     */
    private static boolean dfs(final String current, final Map<String, ConfigOption> optionMap, final Map<String, List<String>> dependencyGraph, final Set<String> visited, final Set<String> visiting, final List<ConfigOption> sortedOptions, final Stack<String> path) {
        visiting.add(current);
        path.push(current);

        //Visit all neighbors of the current node
        for (String neighbor : dependencyGraph.get(current)) {
            if (visiting.contains(neighbor)) {
                path.push(neighbor); //Add the neighbor to the path to show the cycle
                return false; //Found a cycle
            }
            if (!visited.contains(neighbor)) {
                if (!dfs(neighbor, optionMap, dependencyGraph, visited, visiting, sortedOptions, path)) {
                    return false;
                }
            }
        }

        //Mark the current node as visited
        visiting.remove(current);
        visited.add(current);
        //Add the current option to the sorted list
        sortedOptions.add(optionMap.get(current));
        path.pop(); // Remove the current node from the path as we backtrack
        return true;
    }

}
