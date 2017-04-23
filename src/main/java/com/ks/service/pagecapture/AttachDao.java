package com.ks.service.pagecapture;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/18.
 */
public class AttachDao {

    public List<Attachment> getAttachList(int num) {
        String sql = "select tb_url,tb_flag from tb_page where tb_flag='N' and rownum<?";
        List<Attachment> attachments = new ArrayList<Attachment>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = BaseDaoJdbc.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, num);
            rs = ps.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    Attachment m = new Attachment();
                    m.setAttach_id(rs.getString("tb_flag"));
                    m.setAttach_path(rs.getString("tb_url"));
                    attachments.add(m);
                }
                rs.close();
            }
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            BaseDaoJdbc.closeConn(conn, ps, rs);
        }
        return attachments;
    }

    public void setAttach(String val, String attach_id) {
        String sql = "update tb_attachment set attach_ocr_flag='Y',attach_ocr=? where attach_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        int rs = 0;
        try {
            conn = BaseDaoJdbc.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, val);
            ps.setString(2, attach_id);
            rs = ps.executeUpdate();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            BaseDaoJdbc.closeConn(conn, ps);
        }
    }

    public void setUrl(String note_ls_id, String note_id, String content, String title, String zipPath) {
        String sql = "update tb_note_info set note_name=?,url_flag='Y',note_content=?,zip_path=? where note_id=?";
        String sql2 = "UPDATE  tb_note_info_ls set note_name=?,url_flag='Y',note_content=?,zip_path=? where note_ls_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        int rs = 0;
        try {
            conn = BaseDaoJdbc.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, zipPath);
            ps.setString(4, note_id);
            rs = ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement(sql2);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, zipPath);
            ps.setString(4, note_ls_id);
            rs = ps.executeUpdate();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            BaseDaoJdbc.closeConn(conn, ps);
        }
    }

    public void setUrl(String note_ls_id, String note_id) {
        String sql = "update tb_note_info set url=? where note_id=?";
        String sql2 = "UPDATE  tb_note_info_ls set url=? where note_ls_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        int rs = 0;
        try {
            conn = BaseDaoJdbc.getConn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, note_id);
            rs = ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement(sql2);
            ps.setString(1, note_ls_id);
            rs = ps.executeUpdate();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            BaseDaoJdbc.closeConn(conn, ps);
        }
    }

    public List<NoteModel> getNoteList(int num) {
        String sql = "select note_ls_id,note_id,user_id,group_id,url,url_flag from tb_note_info_ls where url is not null and url_flag='N' and rownum<? order by note_ctime desc";
//        String sql = "select note_ls_id,note_id,user_id,group_id,url,url_flag from tb_note_info_ls where url is not null and rownum<? order by note_ctime desc";
        List<NoteModel> attachments = new ArrayList<NoteModel>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = BaseDaoJdbc.getConn();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, num);
            rs = ps.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    NoteModel m = new NoteModel();
                    m.setNote_ls_id(rs.getString("note_ls_id"));
                    m.setNote_id(rs.getString("note_id"));
                    m.setUser_id(rs.getString("user_id"));
                    m.setGroup_id(rs.getString("group_id"));
                    m.setUrl(rs.getString("url"));
                    attachments.add(m);
                }
                rs.close();
            }
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            BaseDaoJdbc.closeConn(conn, ps, rs);
        }
        return attachments;
    }
}
