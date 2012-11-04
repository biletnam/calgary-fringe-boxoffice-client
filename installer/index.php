<?php
    // CHANGE THESE AS NECESSARY
    $app_major = 0;
    $app_minor = 7;
    $app_revision = 37;

    $last_update = 'August 1, 2012';


    // NO CHANGE (at least, you shouldn't have to)
    $filename = 'BoxOffice-' . $app_major . '_' . $app_minor . '_' . $app_revision . '-install.';
    $filename_win = $filename . 'exe';
    $filename_jar = $filename . 'jar';
?>

<HTML>
<HEAD>
    <TITLE>Calgary Fringe Festival - Box Office App Download Page</TITLE>
    <META HTTP-EQUIV="Content-Language" CONTENT="en-ca">
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
    <META NAME="robots" CONTENT="noindex, nofollow">
    <LINK REL="stylesheet" TYPE="text/css" HREF="download.css">
</HEAD>

<BODY><DIV CLASS="mainpage">
    <H1><IMG SRC="../fringe_logo_100x100_trans.png" ALIGN="absmiddle" /> Calgary Fringe Festival - Box Office App Download Page</H1>

    <HR WIDTH="100%" SIZE="1" ALIGN="left" />

    <DIV CLASS="sectionhead">Requirements:</DIV>

    <P>You will need to have Java 1.6 (or newer) installed. This is almost certainly the case already, but
    if you are experiencing difficulties running the install program, you can click
    <A HREF="http://www.java.com/en/download/installed.jsp">here</A> to verify.</P>

    <P>If you do <STRONG>NOT</STRONG> have Java 1.6 (or newer) installed, download and install the latest
    version from the <A HREF="http://java.com/download">java.com website</A>.</P>

    <DIV CLASS="sectionhead">If you are on <STRONG>Windows</STRONG>:</DIV>

    <P>Download and run the Box Office App installer program:
    <UL>
    <LI><A HREF="<?php echo $filename_win; ?>"><IMG SRC="installicon_32x32.png" ALIGN="absmiddle" BORDER="0" /></A>
        <A HREF="<?php echo $filename_win; ?>"><?php echo $filename_win; ?></A></LI>
    </UL>
    </P>

    <P>Shortcut icons will be installed to the start menu and (if you choose) the desktop.</P>

    <DIV CLASS="sectionhead">If you are on <STRONG>MacOS X</STRONG>, <STRONG>Linux</STRONG> or other:</DIV>

    <P>Download and run the Box Office App installer jar file:
    <UL>
    <LI><A HREF="<?php echo $filename_jar; ?>"><IMG SRC="javaicon_32x32.png" ALIGN="absmiddle" BORDER="0" /></A>
        <A HREF="<?php echo $filename_jar; ?>"><?php echo $filename_jar; ?></A></LI>
    </UL>
    </P>

    <P>On <STRONG>MacOS X</STRONG>, you should be able to double-click this file, and it will
    install the application into a folder of your choosing. Inside that folder, there will
    be a file called <TT>BoxOffice.jar</TT> &mdash; double click that file to run the application.
    <EM>(Note I have no way to test this and am not a Mac person, so you're a bit on your own,
    sorry.)</EM></P>

    <P>On <STRONG>Linux</STRONG> (or other Java-supported Unix), download the file to a temporary directory. From a shell, run<BR />
    <TT>&nbsp; &nbsp; $ java -jar <?php echo $filename_jar; ?></TT><BR />
    to run the install program. Install it to a directory of your choosing, then from within that
    directory, run<BR />
    <TT>&nbsp; &nbsp; $ java -jar BoxOffice.jar</TT><BR />
    to run the Box Office application.</P>

    <DIV CLASS="sectionhead">Mobile platforms such as <STRONG>iOS</STRONG>, <STRONG>Android</STRONG>, etc:</DIV>

    <P>Mobile platforms are currently not supported, sorry.</P>

    <DIV CLASS="sectionhead">Help!</DIV>

    <P>On Windows, I have encountered one case where the install program "hangs" on the last step (step 5) and won't
    complete properly. I haven't figured out how to fix that problem yet, however a workaround is to download the
    <A HREF="<?php echo $filename_jar; ?>">jar file</A> instead and double click that. That installation route did
    work in the one case where we ran into problems.</P

    <P>Otherwise, I can almost certainly help with Windows or Linux installation problems and <EM>maybe</EM>
    with MacOS installation problems. Feel free to send me an email: <IMG SRC="hitme.png" ALIGN="absbottom" />,
    and we'll try to work it out.</P>

    <HR WIDTH="100%" SIZE="1" ALIGN="left" />
    <DIV CLASS="update">(Last updated <?php echo $last_update; ?>)</DIV>
</DIV></BODY>
</HTML>
