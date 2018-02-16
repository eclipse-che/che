FROM centos/postgresql-96-centos7
ADD init-che-user-and-run.sh.erb init-che-user.sh.erb /var/lib/pgsql/
RUN cat /var/lib/pgsql/init-che-user.sh.erb | \
    sed -e "/exit 0/d" > /var/lib/pgsql/init-che-user-and-run.sh && \
    echo "exec run-postgresql \"\$@\"" >> /var/lib/pgsql/init-che-user-and-run.sh
USER root
RUN chmod +x /var/lib/pgsql/init-che-user-and-run.sh
USER postgres
CMD ["/var/lib/pgsql/init-che-user-and-run.sh"]
