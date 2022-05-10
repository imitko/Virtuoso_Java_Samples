--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure order_check (in _w_id integer, in _d_id integer)
{
  declare last_o, ol_max, ol_ct, o_max, o_ct, nolines integer;
  select d_next_o_id into last_o from tpcc..district where d_id = _d_id and d_w_id = _w_id;
  select count (*), max (ol_o_id) into ol_ct, ol_max from tpcc..order_line
    where ol_w_id = _w_id and ol_d_id = _d_id;
  select count (*), max (o_id) into o_ct, o_max from tpcc..orders
    where o_w_id = _w_id and o_d_id = _d_id;
  select count (*) into nolines from tpcc..orders where o_w_id = _w_id and o_d_id = _d_id and
    not exists 
      (select 1 from tpcc..order_line where ol_w_id = _w_id and ol_d_id = _d_id and ol_o_id = o_id);
  result_names (last_o, o_max, o_ct, ol_max, ol_ct, nolines);
  result (last_o, o_max, o_ct, ol_max, ol_ct, nolines);
  if (o_ct <> last_o-1 or o_max <> ol_max or o_max <> last_o-1 or nolines <> 0)
    signal ('tpinc', 'inconsistent order counts');
}
