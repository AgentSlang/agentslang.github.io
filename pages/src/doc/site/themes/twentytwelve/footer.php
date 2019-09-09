<?php
/**
 * The template for displaying the footer
 *
 * Contains footer content and the closing of the #main and #page div elements.
 *
 * @package WordPress
 * @subpackage Twenty_Twelve
 * @since Twenty Twelve 1.0
 */
?>
	</div><!-- #main .wrapper -->
	<footer id="colophon" role="contentinfo">
		<div class="site-info">
			<?php do_action( 'twentytwelve_credits' ); ?>
			Copyright and concept: Ovidiu &#x218;erban. All rights reserved. <br/>
			All information released on this website is under GPL and CeCILL-B license. Academic <a href="http://agent.roboslang.org/explore/publications/">citation</a> required for using resources from this site. <br/>
			<a href="<?php echo esc_url( __( 'http://wordpress.org/', 'twentytwelve' ) ); ?>" title="<?php esc_attr_e( 'Semantic Personal Publishing Platform', 'twentytwelve' ); ?>"><?php printf( __( 'Website proudly powered by %s', 'twentytwelve' ), 'WordPress' ); ?></a> <br/>
			Logo concept and design by <a href="http://www.tudorcampean.com/">Tudor Câmpean</a>
		</div><!-- .site-info -->
	</footer><!-- #colophon -->
</div><!-- #page -->

<?php wp_footer(); ?>
</body>
</html>