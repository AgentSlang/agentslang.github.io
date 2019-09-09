<?php
/**
 * The template for displaying Category pages
 *
 * Used to display archive-type pages for posts in a category.
 *
 * @link http://codex.wordpress.org/Template_Hierarchy
 *
 * @package WordPress
 * @subpackage Twenty_Twelve
 * @since Twenty Twelve 1.0
 */

get_header(); ?>

<?php

function display_posts($catID) {
	$args = array('category' => $catID);
	$out = "";

	$myposts = get_posts( $args );
	if (count($myposts) > 0) {
		$out .= "<ol style=\"list-style-type: none;padding: 0px;margin: 0px;\">";
		foreach ( $myposts as $post ) {
			$post_categories = wp_get_post_categories($post->ID);
			if ($post_categories[0] == $catID) {
				$out .= "<li style=\"background-image: url('http://agent.roboslang.org/css/bullet-code-red.png');background-repeat: no-repeat;background-position: 0px -13px; padding-left: 40px; margin-left:30px;\">";
				$out .= "<a href=\"" . get_permalink($post->ID) . "\">" . get_the_title($post->ID) . "</a>";
				$out .= "</li>";
			}
		}
		$out .= "</ol>";
	}
	return $out;
}

function display_category_tree($taxonomy, $parent = 0) {	
    $terms = get_terms($taxonomy, array('parent' => $parent, 'hide_empty' => false));
    //If there are terms, start displaying
    if(count($terms) > 0) {
        //Displaying as a list
        $out = "<ol style=\"list-style-type: none;padding: 0px;margin: 0px;\">";
        //Cycle though the terms
        foreach ($terms as $term) {
            //Secret sauce.  Function calls itself to display child elements, if any
            $out .="<li style=\"background-image: url('http://agent.roboslang.org/css/bullet-orange.png');background-repeat: no-repeat;background-position: 0px -5px; padding-left: 30px;margin-left:15px;\"><a href=\"" . get_category_link($term->term_id) . "\">" . $term->name . "</a>" . display_category_tree($taxonomy, $term->term_id) . "</li>"; 
	
	    $args = array('category' => $term->term_id );

	    $out .= display_posts($term->term_id);            
        }	

        $out .= "</ol>";    
        return $out;
    }
    return;
}

function display_root_tree($taxonomy, $category) {
	echo "<div class=\"categoryTreeList\">";
	echo "<a href=\"" .get_category_link($category->term_id) . "\">" . $category->name . "</a>";
	echo display_category_tree('category', $category->term_id);
	echo display_posts($category->term_id);
	echo "</div>";
}

echo display_root_tree('category', get_category(get_query_var('cat')));
?>

<?php get_sidebar(); ?>
<?php get_footer(); ?>