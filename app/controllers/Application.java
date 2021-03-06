package controllers;

import models.Author;
import models.Comment;
import models.Post;
import play.data.Form;
import play.mvc.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import play.libs.Json;


public class Application extends Controller {

    public static Form<Login> loginForm = Form.form(Login.class);
    public static Form<Comment> commentForm = Form.form(Comment.class);
	
	//JSON
	public static Result allPostJson(){
		List<Post> posts = Post.find.orderBy("postingDate DESC").findList();
		return ok(Json.toJson(posts));
	}
	
	public static Result onePostJson(Integer id){
		Post post = Post.find.byId(id);
		return ok(Json.toJson(post));
	}
	
	public static Result oneAuthorJson(Integer id){
		Author a = Author.find.byId(id);
		return ok(Json.toJson(a));
	}

    public static Result index() {
        return paging(1);
    }

    public static Result paging(Integer page){
        Integer banyakBaris = 2;
        Integer totalData = Post.find.findRowCount();
        Integer totalHalaman = (int) Math.ceil(totalData.doubleValue() / banyakBaris.doubleValue());
        List<Post> listPost = Post.find.orderBy("postingDate DESC").findPagingList(banyakBaris).getPage(page-1).getList();

        int current = page;
        int begin = Math.max(1, current - banyakBaris);
        int end = Math.min(begin + 4, totalHalaman);
        List<Integer> listNum = new ArrayList<>();
        for(int i=begin;i<end;i++){
            listNum.add(i);
        }
        return ok(views.html.index.render(listPost, current, listNum,totalHalaman, totalData));
    }
	
	public static Result authorProfile(Integer id){
		Author a = Author.find.byId(id);
		Integer totalData = Post.find.where().eq("author", a).findRowCount();
		return ok(views.html.authorprofile.render(a, totalData));
	}

    public static Result postDetail(Integer id){
        Post p = Post.find.byId(id);
        List<Comment> commentList = Comment.findCommentByPost(p);
        int commentSize = commentList.size();
		session().clear();
		session("post", p.getId().toString());
        return ok(views.html.postdetail.render(p, commentList, commentSize, commentForm));
    }

    public static Result addCommentFinish(){
        Form<Comment> cf = commentForm.bindFromRequest();
        Comment c = new Comment();
		String postId = session("post");
		Post p = Post.find.byId(new Integer(postId));
        c.setPost(p);
        c.setCommentatorName(cf.get().getCommentatorName());
        c.setCommentContent(cf.get().getCommentContent());
        c.setCommentDate(new Date());
        c.save();
		session().remove("post");
        return redirect(routes.Application.postDetail(c.getPost().getId()));
    }
	
	public static Result getAuthorPicture(String path){
		return ok();
	}

    public static Result login(){
        return ok(views.html.login.render(loginForm));
    }

    public static Result logout(){
        session().remove("email");
        flash("success", "You've been logged out..");
        return redirect(routes.Application.login());
    }

    public static Result authenticate(){
        Form<Login> lf = loginForm.bindFromRequest();
        if(lf.hasErrors()){
			flash("errors", "Invalid email or password !!");
            return badRequest(views.html.login.render(loginForm));
        }else {
            session().remove("email");
            session("email", lf.get().getEmail());
            return redirect(routes.Dashboard.index());
        }
    }

    public static class Login{
        private String email;
        private String password;

        public String validate(){
            if(Author.authenticate(getEmail(), getPassword()) == null){
                return "Invalid email or password !!";
            }
            return null;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
